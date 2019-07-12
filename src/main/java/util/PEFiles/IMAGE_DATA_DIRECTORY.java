package util.PEFiles;
import java.io.PrintStream;

public class IMAGE_DATA_DIRECTORY extends HEADER{
  /**********************************************
   *typedef struct _IMAGE_DATA_DIRECTORY {      *
   *  DWORD   VirtualAddress;             : 0x00*
   *  DWORD   Size;                       : 0x04*
   *}                                           *
   * #define IMAGE_NUMBEROF_DIRECTORY_ENTRIES 16*
   **********************************************/
  public int VirtualAddress;
  public int Size;
  
  public IMAGE_DATA_DIRECTORY(byte[] data){ create(data, 0); }
  public IMAGE_DATA_DIRECTORY(char[] data){ create(toByteArray(data), 0); }
  public IMAGE_DATA_DIRECTORY(byte[] data, int offset){ create(data, offset); }
  public IMAGE_DATA_DIRECTORY(char[] data, int offset){ create(toByteArray(data), offset); }
  public void create(byte[] data, int offset){
    VirtualAddress = getInt(data, offset+0x00);
    Size           = getInt(data, offset+0x04);
  }
  public void print(PrintStream out){ print(out, 0); }
  public void print(PrintStream out, int indent){
    String i = "";
    for(int x = 0; x < indent; x++) i += "  ";
    out.println(i+"IMAGE_DATA_DIRECTORY");
    out.println(i+"  Virtual Address: 0x" + hex(VirtualAddress));
    out.println(i+"  Size:            0x" + hex(Size));
    out.println(i+"END IMAGE_DATA_DIRECTORY");
  }
}