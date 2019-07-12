package util.PEFiles;

public class HEADER{
  public short getShort(byte[] data, int offset){
    return (short)((data[offset]          & 0x00FF) |
                  ((data[offset+1] << 8)  & 0xFF00));
  }
  public int getInt(byte[] data, int offset){
    return ( data[offset]          & 0x000000FF) |
           ((data[offset+1] << 8)  & 0x0000FF00) |
           ((data[offset+2] << 16) & 0x00FF0000) |
           ((data[offset+3] << 24) & 0xFF000000);
  }
  public String getString(byte[] data, int offset){ return getString(data, offset, data.length-offset); }
  public String getString(byte[] data, int offset, int maxlen){
    int start = offset;
    while(data[offset] != 0 && offset < data.length && (offset-start) < maxlen) offset++;
    byte[] tmp = new byte[offset-start];
    System.arraycopy(data, start, tmp, 0, offset-start);
    return new String(tmp);
  }
  
  public byte[] toByteArray(char[] data){
    byte[] ret = new byte[data.length];
    for(int x = 0; x < ret.length; x++)
      ret[x] = (byte)data[x];
    return ret;
  }
  public String hex(short i){
    String ret = Integer.toHexString(i&0x0000FFFF);
    while(ret.length() < 4) ret = "0" + ret;
    return ret;
  }
  public String hex(int i){
    String ret = Integer.toHexString(i);
    while(ret.length() < 8) ret = "0" + ret;
    return ret;
  }
}