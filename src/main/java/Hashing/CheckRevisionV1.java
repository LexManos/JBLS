package Hashing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import util.*;

/** This takes care of the CheckRevision() for the main game files of any program.
 * This is done to prevent tampering and to make sure the version is correct.
 * <P>
 * This function is generally slow because it has to read through the entire
 * files.  The majority of the time is spent in i/o, but I've tried to optimize
 * this as much as possible.
 * @author iago/Hdx
 */
public class CheckRevisionV1
{
    /** These are the hashcodes for the various .mpq files. */
    private static final int hashcodes[] = { 0xE7F4CB62, 0xF6A14FFC, 0xAA5504AF, 0x871FCDC2, 0x11BF6A18, 0xC57292E6, 0x7927D27E, 0x2FEC8733 };
    
    private static final String fastFormulas[] = { "A=A.S", "B=B.C", "C=C.A", "A=A.B" };
    
    private static int    Version[][]     = new int[3][0x0C];
    private static Buffer    Info[][]     = new Buffer[3][0x0C];

    /** Stores some past results */
    private static Hashtable<String, CheckrevisionResults> crCache = new Hashtable<String, CheckrevisionResults>();
    private static int crCacheHits = 0;
    private static int crCacheMisses = 0;

    
    public static void clearCache(){
      Version = new int[3][0x0c];
      Info = new Buffer[3][0x0c];
      crCacheHits = 0;
      crCacheMisses = 0;
      crCache = new Hashtable<String, CheckrevisionResults>();
      System.gc();
    }
    /** Does the actual version check.
     * @param versionString The version string.  This is recieved from Battle.net in 0x50 (SID_AUTH_INFO) and
     * looks something like "A=5 B=10 C=15 4 A=A+S B=B-A A=S+B C=C-B".
     * @param prod The product this is to be performed on, In BNLS numbering.
     * @param mpq The Full MPQ name: ver-PLAT-#.mpq PLATver#.mpq
     * @throws FileNotFoundException If the datafiles aren't found.
     * @throws IOException If there is an error reading from one of the datafiles.
     * @return An instance of the CheckrevisionResults class after performing the requested checkrevision.
     */
    public static CheckrevisionResults checkRevision(String versionString, int prod, byte plat, String mpq) throws FileNotFoundException, IOException
    {
        if(prod > 0x0B || plat > 3 || prod < 0 || plat < 0) return null;
        
        CheckrevisionResults cacheHit = (CheckrevisionResults)crCache.get(versionString + mpq + prod + plat);
        if(cacheHit != null){
            Out.println("CREV", "CheckRevision cache hit: " + crCacheHits + " hits, " + crCacheMisses + " misses.");
            crCacheHits++;
            return cacheHit;
        }
        crCacheMisses++;

        StringTokenizer tok = new StringTokenizer(versionString, " ");

        long a=0,b=0,c=0;

        for(int x = 0; x<3; x++){
          String seed = tok.nextToken();
          if(seed.toLowerCase().startsWith("a=") == true) a = Long.parseLong(seed.substring(2));
          if(seed.toLowerCase().startsWith("b=") == true) b = Long.parseLong(seed.substring(2));
          if(seed.toLowerCase().startsWith("c=") == true) c = Long.parseLong(seed.substring(2));
        }
        tok.nextToken();
        
        char op[] = new char[4];
        String formula;
        int checksum = 0;
        
        for (int x = 0; x < 4; x++) {
        	if (tok.hasMoreTokens()) {
        		formula = tok.nextToken();
        		op[x] = formula.charAt(3);
        		
        		if (formula.matches(fastFormulas[x]) == false && checksum == 0) {
        			checksum = checkRevisionSlow(versionString, prod, plat, mpq);
        			break;
        		}
        	}
        }

        String[] files = getFiles(prod, plat);

        if(checksum == 0){
          // Now we actually do the hashing for each file
          // Start by hashing A by the hashcode
          int mpqNum = 0;
          if(mpq.substring(0, 3).equals("ver")) mpqNum = mpq.charAt(9) - 0x30;
          if(mpq.substring(4, 7).equals("ver")) mpqNum = mpq.charAt(7) - 0x30;
          
          a ^= hashcodes[mpqNum];

          for(int i = 0; i < files.length; i++)
          {
            File currentFile = new File(files[i]);

            byte []data = readFile(currentFile);
            for(int j = 0; j < data.length; j += 4)
            {
                int s = 0;
                s |= ((data[j+0] << 0) & 0x000000ff);
                s |= ((data[j+1] << 8) & 0x0000ff00);
                s |= ((data[j+2] << 16) & 0x00ff0000);
                s |= ((data[j+3] << 24) & 0xff000000);

                switch (op[0]) {
                    case '^': a ^= s; break;
                    case '+': a += s; break;
                    case '-': a -= s; break;
                    case '*': a *= s; break;
                    case '/': a /= s; break;
                }
                switch (op[1]) {
                    case '^': b ^= c; break;
                    case '+': b += c; break;
                    case '-': b -= c; break;
                    case '*': b *= c; break;
                    case '/': b /= c; break;
                }
                switch (op[2]) {
                    case '^': c ^= a; break;
                    case '+': c += a; break;
                    case '-': c -= a; break;
                    case '*': c *= a; break;
                    case '/': c /= a; break;
                }
                switch (op[3]) {
                    case '^': a ^= b; break;
                    case '+': a += b; break;
                    case '-': a -= b; break;
                    case '*': a *= b; break;
                    case '/': a /= b; break;
                }
            }
          }
          checksum = (int)c;
        }
        
        if(Version[plat][prod] == 0) Version[plat][prod] = getVersion(files, prod);
        if(   Info[plat][prod] == null) {
          String info = getInfo(files);
          if(info != null) Info[plat][prod] = new Buffer(info.getBytes());
          Info[plat][prod].addByte((byte)0);
        }
        
        if(checksum == 0 || Version[plat][prod] == 0 || Info[plat][prod] == null) return null;

        CheckrevisionResults result = new CheckrevisionResults(Version[plat][prod], checksum, Info[plat][prod]);
        crCache.put(versionString + mpq + prod + plat, result);

        return result;
    }


    /** This is an alternate implementation of CheckRevision.  It it slower (about 2.2 times slower), but it can handle
     * weird version strings that Battle.net would never send.  Battle.net's version strings are _always_ in the form:
     * A=x B=y C=z 4 A=A?S B=B?C C=C?A A=A?B:
     * C=1151438134 A=2788537374 B=2369803856 4 A=A-S B=B+C C=C^A A=A^B
     * A=1054538081 B=741521288 C=797042342 4 A=A^S B=B-C C=C^A A=A+B
     *
     * If, for some reason, the string in checkRevision() doesn't match up, this will run.
     *
     * @param versionString The version string.  This is recieved from Battle.net in 0x50 (SID_AUTH_INFO) and
     * looks something like "A=5 B=10 C=15 4 A=A+S B=B-A A=S+B C=C-B".
     * @param files The array of files we're checking.  Generally the main game files, like
     * Starcraft.exe, Storm.dll, and Battle.snp.
     * @param mpqNum The number of the mpq file, from 1..7.
     * @throws FileNotFoundException If the datafiles aren't found.
     * @throws IOException If there is an error reading from one of the datafiles.
     * @return The 32-bit CheckRevision hash.
     */
    private static int checkRevisionSlow(String versionString, int prod, byte plat, String mpq) throws FileNotFoundException, IOException
    {
        System.out.println("Warning: using checkRevisionSlow for version string: " + versionString);

        // First, parse the versionString to name=value pairs and put them
        // in the appropriate place
        int[] values = new int[4];
        int[] opValueDest = new int[4];
        int[] opValueSrc1 = new int[4];
        char[] operation = new char[4];
        int[] opValueSrc2 = new int[4];

        // Break this apart at the spaces
        StringTokenizer s = new StringTokenizer(versionString, " ");
        int currentFormula = 0;
        while(s.hasMoreTokens()){
            String thisToken = s.nextToken();
            // As long as there is an '=' in the string
            if(thisToken.indexOf('=') > 0){
                // Break it apart at the '='
                StringTokenizer nameValue = new StringTokenizer(thisToken, "=");
                if(nameValue.countTokens() != 2) return 0;

                int variable = getNum(nameValue.nextToken().charAt(0));

                String value = nameValue.nextToken();

                // If it starts with a number, assign that number to the appropriate variable
                if(Character.isDigit(value.charAt(0))){
                    values[variable] = Integer.parseInt(value);
                }else{
                    opValueDest[currentFormula] = variable;

                    opValueSrc1[currentFormula] = getNum(value.charAt(0));
                    operation[currentFormula] = value.charAt(1);
                    opValueSrc2[currentFormula] = getNum(value.charAt(2));

                    currentFormula++;
                }
            }
        }

        // Now we actually do the hashing for each file
        // Start by hashing A by the hashcode
        int mpqNum = 0;
        if(mpq.substring(0, 3).equals("ver")) mpqNum = mpq.charAt(9) - 0x30;
        if(mpq.substring(4, 7).equals("ver")) mpqNum = mpq.charAt(7) - 0x30;
        System.out.println(mpq + "  " + mpqNum);
          
        values[0] ^= hashcodes[mpqNum];
        String[] files = getFiles(prod, plat);
        
        for(int i = 0; i < files.length; i++)
        {
            File currentFile = new File(files[i]);

            byte []data = readFile(currentFile);

            for(int j = 0; j < data.length; j += 4)
            {
                values[3] = 0;
                values[3] |= ((data[j+0] << 0) & 0x000000FF);
                values[3] |= ((data[j+1] << 8) & 0x0000ff00);
                values[3] |= ((data[j+2] << 16) & 0x00ff0000);
                values[3] |= ((data[j+3] << 24) & 0xff000000);

                for(int k = 0; k < currentFormula; k++){
                    switch(operation[k]){
                        case '^': values[opValueDest[k]] = values[opValueSrc1[k]] ^ values[opValueSrc2[k]]; break;
                        case '+': values[opValueDest[k]] = values[opValueSrc1[k]] + values[opValueSrc2[k]]; break;
                        case '-': values[opValueDest[k]] = values[opValueSrc1[k]] - values[opValueSrc2[k]]; break;
                        case '*': values[opValueDest[k]] = values[opValueSrc1[k]] * values[opValueSrc2[k]]; break;
                        case '/': values[opValueDest[k]] = values[opValueSrc1[k]] / values[opValueSrc2[k]]; break;
                    }
                }
             }
        }
        return values[2];
    }

    /** Converts the parameter to which number in the array it is, based on A=0, B=1, C=2, S=3.
     * @param c The character letter.
     * @return The array number this is found at.
     */
    private static int getNum(char c)
    {
        c = Character.toUpperCase(c);
        if(c == 'S')
            return 3;

        return c - 'A';
    }

    public static byte []readFile(File file) throws IOException{
        int length = (int) file.length();
        byte []ret = new byte[(length % 1024) == 0 ? length : (length / 1024 * 1024)];

        InputStream in = new FileInputStream(file);
        in.read(ret);
        in.close();

        return ret;
    }
    
    
    public static String[] getFiles(int prod, byte plat){
      List<String> ret = new ArrayList<String>();
      
      if(prod < 0) return null;
      if(prod > Constants.prods.length + 1) return null;
      
      switch(plat){
        case Constants.PLATFORM_INTEL:
          String[] files = Constants.IX86files[prod - 1];
          for (int i = 1; i < files.length - 1; i++) {
            if (!files[i].equalsIgnoreCase("NULL") && files[i].length() > 0)
          	  ret.add(files[0] + files[i]);
            }
          break;
      /*case PLATFORM_POWERPC:
          break;
      case PLATFORM_MACOSX:
        break;*/
        default: return null;
      }
      return ret.toArray(new String[0]);
    }
    
    
    /** Compiles BNET Exe Info of a given game set
     * @return Exe Info String
     * @param files - file list
     * */
     private static String getInfo(String[] files){
       File f = new File(files[0]);
       if(!f.exists()) return null;
       Calendar c = Calendar.getInstance();
       c.setTime(new Date(f.lastModified()));
       
       StringBuffer exeInfo = new StringBuffer();
       exeInfo.append(f.getName()).append(" ");
       exeInfo.append(PadString.padNumber(c.get(Calendar.MONTH) + 1, 2)).append("/");
       exeInfo.append(PadString.padNumber(c.get(Calendar.DAY_OF_MONTH), 2)).append("/");
       exeInfo.append(PadString.padNumber((c.get(Calendar.YEAR) % 100), 2)).append(" ");
       exeInfo.append(PadString.padNumber(c.get(Calendar.HOUR_OF_DAY), 2)).append(":");
       exeInfo.append(PadString.padNumber(c.get(Calendar.MINUTE), 2)).append(":");
       exeInfo.append(PadString.padNumber(c.get(Calendar.SECOND), 2)).append(" ");
       exeInfo.append(f.length());
       
       return exeInfo.toString();
    }

    public static int getVersion(String[] files, int prod){
      int ver = 0;
      try{
        ver = PE.getVersion(files[0], false);
        if(prod == Constants.PRODUCT_WARCRAFT3 || prod == Constants.PRODUCT_THEFROZENTHRONE){
          ver = (ver & 0xFF000000) >>> 24 |
                (ver & 0x00FF0000) >> 8 |
                (ver & 0x0000FF00) << 8 |
                (ver & 0x000000FF) << 24;
        }
        return ver;
      }catch(FileNotFoundException e){
        Out.error("HashMain", "Hash Exception(Exe version): \r\n" +
        "[getVersion] File Not Found/Accessible (" + files[0] + ")");
      }catch(IOException e){
        Out.error("HashMain", "Hash Exception(Exe Version): [getVersion] IOException");
      }
      return 0;
    }
}
