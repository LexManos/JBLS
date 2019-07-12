package Hashing;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import util.*;

/**
 *
 * This is the main Class that provides hashing functions,
 * CheckRevision, Exe Info, etc provided by BNLS.
 *
 * Static Methods Allow it to be accessible by any thread
 *
 * @throws HashException - If error caused by Hashing or retrieval (bad key, etc)
 *
 */
public class HashMain {

  //Only compilied once(as needed), then stored
  public  static int    CRevChecks[] = new int[0x0B];

  public static int WAR3KeysHashed=0;
  public static int STARKeysHashed=0;
  public static int D2DVKeysHashed=0;


  /** Picks appropriate hashing method based on length, and
   * hashes the CD-Key.
   * @param clientToken ClientToken used in hash specified by Client or JBLS
   * @param serverToken ServerToken used in hash specified by BNET server
   * @return HashedKey(in a Buffer) - 9 DWORDS
   * @throws HashException If Invalid Key
   * */
  public static Buffer hashKey(int clientToken, int serverToken, String key) throws HashException{
    switch(key.length()){
      case 13:
        if(Constants.displayParseInfo) Out.println("Hash",">>> STAR Key Hashed");
        STARKeysHashed++;
        return hashSCKey(clientToken, serverToken, key);
      case 16:
        if(Constants.displayParseInfo) Out.println("Hash",">>> WAR2/D2 Key Hashed");
        D2DVKeysHashed++;
        return hashD2Key(clientToken, serverToken, key);
      case 26:
        if(Constants.displayParseInfo) Out.println("Hash",">>> WAR3/FT Key Hashed");
        WAR3KeysHashed++;
        return hashWAR3Key(clientToken, serverToken, key);
    }
    throw new HashException("Invalid Key Length");
  }

  private static Buffer hashD2Key(int clientToken, int serverToken, String key) throws HashException{
    D2KeyDecode d2=new D2KeyDecode(key);
    Buffer ret = new Buffer();
    ret.addDWord(key.length());
    ret.addDWord(d2.getProduct());
    ret.addDWord(d2.getVal1());
    ret.addDWord(0);
    int hashedKey[]=d2.getKeyHash(clientToken, serverToken);
    for(int i = 0; i < 5; i++) ret.addDWord(hashedKey[i]);
    return ret;
  }

  private static Buffer hashSCKey(int clientToken, int serverToken, String key) throws HashException{
    SCKeyDecode sc=new SCKeyDecode(key);
    Buffer ret = new Buffer();
    ret.addDWord(key.length());
    ret.addDWord(sc.getProduct());
    ret.addDWord(sc.getVal1());
    ret.addDWord(0);
    int hashedKey[]=sc.getKeyHash(clientToken, serverToken);
    for(int i = 0; i < 5; i++) ret.addDWord(hashedKey[i]);
    return ret;
  }

  private static Buffer hashWAR3Key(int clientToken, int serverToken, String key){
    War3Decode w3=new War3Decode(key);
    Buffer ret = new Buffer();
    ret.addDWord(key.length());
    ret.addDWord(w3.getProduct());
    ret.addDWord(w3.getVal1());
    ret.addDWord(0);
    int hashedKey[]=w3.getKeyHash(clientToken, serverToken);
    for(int i = 0; i < 5; i++) ret.addDWord(hashedKey[i]);
    return ret;
  }
  
  public static int getVerByte(int prod){
    if(prod <= 0) return 0;
    if(prod > Constants.prods.length) return 0;
    if (Constants.displayParseInfo) Out.info("JBLS", ">>> [" + Constants.prods[prod-1] + "] Verbyte");
    return Constants.IX86verbytes[prod-1];
  }

  public static CheckrevisionResults getChecksum(int prod, String formula, int dll){
    return getRevision(prod, formula, "ver-IX86-" + dll + ".mpq", Constants.PLATFORM_INTEL, 2);
  }
  
  public static CheckrevisionResults getRevision(int prod, String formula, String dll, byte platform, int ver){
    String[] files = null;
    try{
      CRevChecks[prod-1]++;
      if (Constants.displayParseInfo) Out.info("HashMain", ">>> [" + Constants.prods[prod-1] + "] Version Check V"+ver);
      switch(ver){
        case 1: files = CheckRevisionV1.getFiles(prod, platform); return CheckRevisionV1.checkRevision(formula, prod, platform, dll);
        case 2: files = CheckRevisionV1.getFiles(prod, platform); return CheckRevisionV2.checkRevision(formula, prod, platform, dll);
        case 3: files = CheckRevisionV3.getFiles(prod, platform); return CheckRevisionV3.checkRevision(formula, prod, platform, dll);
        case 4: files = new String[0]; return CheckRevisionV4.checkRevision(formula, prod, platform, dll);
        default:files = CheckRevisionV1.getFiles(prod, platform); return CheckRevisionV2.checkRevision(formula, prod, platform, dll);
      }
    }catch(FileNotFoundException e){
      String fileList = "";
      for (int i = 0; i < files.length; i++) {
        if (files[i].isEmpty())
          continue;

        Path path = Paths.get(files[i]);

        if (!Files.exists(path)) {
          if (!fileList.isEmpty())
            fileList += ", ";

          fileList += files[i];
        }
      }

      if (ver == 3) {
        String fullArchivePath = Constants.ArchivePath + dll.replaceAll("mpq", "dll");

        if (!Files.exists(Paths.get(fullArchivePath))) {
          if (!fileList.isEmpty())
            fileList += ", ";

            fileList += fullArchivePath;
        }
      }

      Out.error("HashMain", "Hash Exception(version check): \r\n" +
            "[CheckRevision] Files Not Found/Accessible (" + Constants.prods[prod-1] + ") (" + fileList + ")");
    }catch(IOException e){
      Out.error("HashMain", "Hash Exception(version check): [CheckRevision] IOException (" + Constants.prods[prod-1] + ")");
    }
    return null;
  }

  public static CheckrevisionResults getRevision(int prod, String formula, String dll, long filetime){
    if(dll.matches("ver-IX86-[0-7].mpq") == true) return getRevision(prod, formula, dll, Constants.PLATFORM_INTEL, 2);
    if(dll.matches("ver-XMAC-[0-7].mpq") == true) return getRevision(prod, formula, dll, Constants.PLATFORM_MACOSX, 2);
    if(dll.matches("ver-PMAC-[0-7].mpq") == true) return getRevision(prod, formula, dll, Constants.PLATFORM_POWERPC, 2);

    if(dll.matches("IX86ver[0-7].mpq") == true) return getRevision(prod, formula, dll, Constants.PLATFORM_INTEL, 1);
    if(dll.matches("XMACver[0-7].mpq") == true) return getRevision(prod, formula, dll, Constants.PLATFORM_MACOSX, 1);
    if(dll.matches("PMACver[0-7].mpq") == true) return getRevision(prod, formula, dll, Constants.PLATFORM_POWERPC, 1);

    if(dll.matches("lockdown-IX86-[0-1][0-9].mpq") == true) return getRevision(prod, formula, dll, Constants.PLATFORM_INTEL, 3);

    if(dll.matches("CheckRevision.mpq") == true) return getRevision(prod, formula, dll, Constants.PLATFORM_INTEL, 4);
    if(dll.matches("CheckRevisionD1.mpq") == true) return getRevision(prod, formula, dll, Constants.PLATFORM_INTEL, 4);
    
    Out.info("CHSUM", "Unknown archive: " + dll + ", Filetime: 0x" + Long.toHexString(filetime));
    return null;
  }
}
