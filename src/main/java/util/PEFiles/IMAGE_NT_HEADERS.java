package util.PEFiles;
import java.io.PrintStream;

public class IMAGE_NT_HEADERS extends HEADER{
  /**************************************************
   *typedef struct _IMAGE_NT_HEADERS {              *
   *  DWORD Signature;                        : 0x00*
   *  IMAGE_FILE_HEADER FileHeader;           : 0x04*
   *  IMAGE_OPTIONAL_HEADER32 OptionalHeader; : 0x18*
   *}                                               *
   **************************************************/
  public int Signature;
  public IMAGE_FILE_HEADER FileHeader;
  public IMAGE_OPTIONAL_HEADER32 OptionalHeader;
  
  public IMAGE_NT_HEADERS(byte[] data){ create(data, 0); }
  public IMAGE_NT_HEADERS(char[] data){ create(toByteArray(data), 0); }
  public IMAGE_NT_HEADERS(byte[] data, int offset){ create(data, offset); }
  public IMAGE_NT_HEADERS(char[] data, int offset){ create(toByteArray(data), offset); }
  public void create(byte[] data, int offset){
    Signature = getInt(data, offset);
    FileHeader = new IMAGE_FILE_HEADER(data, offset+0x04);
    OptionalHeader = new IMAGE_OPTIONAL_HEADER32(data, offset+0x18);
  }
  public void print(PrintStream out){ print(out, 0); }
  public void print(PrintStream out, int indent){
    String i = "";
    for(int x = 0; x < indent; x++) i += "  ";
    out.println(i+"IMAGE_NT_HEADERS");
    out.println(i+"  Signature: 0x" + hex(Signature));
    FileHeader.print(out, indent+1);
    OptionalHeader.print(out, indent+1);
    out.println(i+"END IMAGE_NT_HEADERS");
  }
}