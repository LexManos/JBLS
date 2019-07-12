package util.PEFiles;
import java.util.*;

public class ImportAddressManager{
  private static Hashtable<String, Integer> addresses = new Hashtable<String, Integer>();
  private static Hashtable<String, String> forwards = new Hashtable<String, String>();
  public static int getAddress(String function){
    Integer ret = addresses.get(function.toLowerCase());
    String forward = forwards.get(function.toLowerCase());
    if(ret != null) return ret.intValue();
    if(forward != null) return getAddress(forward.toLowerCase());
    
    String library = function.substring(0, function.indexOf("."));
    loadLibrary("c:\\winnt\\system32\\" + library + ".dll", library);
    
    ret = addresses.get(function.toLowerCase());
    forward = forwards.get(function.toLowerCase());
    if(ret != null) return ret.intValue();
    if(forward != null) return getAddress(forward.toLowerCase());
    
    return 0;
  }
  
  public static void loadLibrary(String file, String prepend){
    PEFile pe = new PEFile(file);
    if(!pe.loaded){
      System.out.println("Failed to load: " + file + " file not found.");
      return;
    }
    IMAGE_EXPORT_DIRECTORY dir = new IMAGE_EXPORT_DIRECTORY(pe.getSectionsData(), pe.ntheader.OptionalHeader.DataDirectory[PEFile.EXPORT_TABLE].VirtualAddress);
//    dir.print(System.out);
    int[] addrs = new int[dir.NumberOfFunctions];
    short[] ordinals = new short[dir.NumberOfNames];
    String[] names = new String[dir.NumberOfNames];
    for(int x = 0; x < dir.NumberOfFunctions; x++)
      addrs[x] = pe.getIntSection(dir.AddressOfFunctions+(x*4));
    
    for(int x = 0; x < dir.NumberOfNames; x++){
      int nameAddr = pe.getIntSection(dir.AddressOfNames+(x*4));
      ordinals[x] = pe.getShortSection(dir.AddressOfOrdinals+(x*2));
      names[x] = pe.getStringSection(nameAddr);
    }
    int low = pe.ntheader.OptionalHeader.DataDirectory[PEFile.EXPORT_TABLE].VirtualAddress;
    int high = low + pe.ntheader.OptionalHeader.DataDirectory[PEFile.EXPORT_TABLE].Size;
       
    for(int x = 0; x <  dir.NumberOfNames; x++){
      if(addrs[x] > low && addrs[x] < high){
        forwards.put((prepend+"."+names[x]).toLowerCase(), pe.getStringSection(addrs[x]));
  //      System.out.println((prepend+"."+names[x]).toLowerCase() + 
  //      "->" + pe.getStringSection(addrs[x]) + " | " + 
  //      Integer.toHexString(addrs[x]) + " | " + Integer.toHexString(pe.ntheader.OptionalHeader.ImageBase));
      }else{
        addresses.put((prepend+"."+names[x]).toLowerCase(), new Integer(addrs[x]+pe.ntheader.OptionalHeader.ImageBase));
        //System.out.println((prepend+"."+names[x]).toLowerCase() + 
        //"->" + Integer.toHexString((addrs[x]+pe.ntheader.OptionalHeader.ImageBase)) + 
        //" | " + Integer.toHexString(addrs[x]) + 
        //" | " + Integer.toHexString(pe.ntheader.OptionalHeader.ImageBase));
      }
    }
  }
}