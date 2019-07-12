package util;
import java.io.*;

public class seed_finder{

  //81 f1 FF FF FF FF 35 FF FF FF FF 89 4d 0c 89 45 10 6a 08
  public static byte[] lockdown_values = new byte[]{(byte)0x81, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x35, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x6a, (byte)0x08};
  public static int[] lockdown_offsets = new int[]{2, 7};

  public static int[] find_seeds(String file, byte[] values, int[] offsets){  
//    System.out.println("seed_find: " + file);
    int[] ret = new int[offsets.length];
    byte[] data = readFile(file);  
    if(data == null) return ret;
    for(int i = 0; i < data.length - values.length; i++){
      boolean found = true;
      for(int j = 0; j < values.length && found; j++)
        if(values[j] != (byte)0xFF && (data[i + j] != values[j])) found = false;
    
      if(found){
        //System.out.print(file);
        for(int x = 0; x < offsets.length; x++){
          ret[x] = ((data[i+offsets[x]+0] << 0)  & 0x000000FF) |
               ((data[i+offsets[x]+1] << 8)  & 0x0000FF00) |
               ((data[i+offsets[x]+2] << 16) & 0x00FF0000) |
               ((data[i+offsets[x]+3] << 24) & 0xFF000000);
          //System.out.print(" " + Integer.toHexString(ret[x]));
        }
        //System.out.println("");
        return ret;
      }
    }
    return ret;
  }
  public static byte[] readFile(String file){
    try{
      byte[] ret = new byte[(int)(new File(file)).length()];
      InputStream in = new FileInputStream(file);
      in.read(ret);
      in.close();
      return ret;
    }catch(Exception e){ return null; }
  }
}