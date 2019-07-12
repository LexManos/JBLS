package util.PEFiles;
import java.io.PrintStream;

public class IMAGE_EXPORT_DIRECTORY extends HEADER{
  /******************************************
   *typedef struct _IMAGE_EXPORT_DIRECTORY {*
   *  DWORD   Characteristics;       : 0x00 *
   *  DWORD   TimeDateStamp;         : 0x04 *
   *  WORD    MajorVersion;          : 0x08 *
   *  WORD    MinorVersion;          : 0x0a *
   *  DWORD   Name;                  : 0x0c *
   *  DWORD   Base;                  : 0x10 *
   *  DWORD   NumberOfFunctions;     : 0x14 *
   *  DWORD   NumberOfNames;         : 0x18 *
   *  DWORD   AddressOfFunctions;    : 0x1c *
   *  DWORD   AddressOfNames;        : 0x20 *
   *  DWORD   AddressOfNameOrdinals; : 0x24 *
   *}                                       *
   ******************************************/
  public int Characteristics;
  public int TimeDateStamp;
  public short MajorVersion;
  public short MinorVersion;
  public int Name;
  public int Base;
  public int NumberOfFunctions;
  public int NumberOfNames;
  public int AddressOfFunctions;
  public int AddressOfNames;
  public int AddressOfOrdinals;
  
  public IMAGE_EXPORT_DIRECTORY(byte[] data){ create(data, 0); }
  public IMAGE_EXPORT_DIRECTORY(char[] data){ create(toByteArray(data), 0); }
  public IMAGE_EXPORT_DIRECTORY(byte[] data, int offset){ create(data, offset); }
  public IMAGE_EXPORT_DIRECTORY(char[] data, int offset){ create(toByteArray(data), offset); }
  public void create(byte[] data, int offset){
    Characteristics    = getInt(data, offset+0x00);
    TimeDateStamp      = getInt(data, offset+0x04);
    MajorVersion       = getShort(data, offset+0x08);
    MinorVersion       = getShort(data, offset+0x0a);
    Name               = getInt(data, offset+0x0c);
    Base               = getInt(data, offset+0x10);
    NumberOfFunctions  = getInt(data, offset+0x14);
    NumberOfNames      = getInt(data, offset+0x18);
    AddressOfFunctions = getInt(data, offset+0x1c);
    AddressOfNames     = getInt(data, offset+0x20);
    AddressOfOrdinals  = getInt(data, offset+0x24);
  }
  public void print(PrintStream out){ print(out, 0); }
  public void print(PrintStream out, int indent){
    String i = "";
    for(int x = 0; x < indent; x++) i += "  ";
    out.println(i+"IMAGE_EXPORT_DIRECTORY");
    out.println(i+"  Characteristics:   0x" + hex(Characteristics));
    out.println(i+"  Time Date Stamp:   0x" + hex(TimeDateStamp));
    out.println(i+"  Version:           " + MajorVersion + "." + MinorVersion);
    out.println(i+"  Name:              0x" + hex(Name));
    out.println(i+"  Base:              " + Base);
    out.println(i+"  Function Count:    " + NumberOfFunctions);
    out.println(i+"  Name Count:        " + NumberOfNames);
    out.println(i+"  Functions Address: 0x" + hex(AddressOfFunctions));
    out.println(i+"  Names Address:     0x" + hex(AddressOfNames));
    out.println(i+"  Ordinals Address:  0x" + hex(AddressOfOrdinals));
    out.println(i+"END IMAGE_EXPORT_DIRECTORY");
  }
}