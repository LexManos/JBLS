package util.PEFiles;
import java.io.PrintStream;

public class IMAGE_IMPORT_DIRECTORY extends HEADER{
  /**********************************************
   *typedef struct _IMAGE_IMPORT_DIRECTORY {    *
   *  DWORD   AddressOfImportLookupTable; : 0x00*
   *  DWORD   TimeDateStamp;              : 0x04*
   *  DWORD   ForwarderChain;             : 0x08*
   *  DWORD   AddressOfNames;             : 0x0c*
   *  DWORD   AddressOfImportTable;       : 0x10*
   *}                                           *
   **********************************************/
  public int AddressOfImportLookupTable;
  public int TimeDateStamp;
  public int ForwarderChain;
  public int AddressOfName;
  public int AddressOfImportTable;
    
  public IMAGE_IMPORT_DIRECTORY(byte[] data){ create(data, 0); }
  public IMAGE_IMPORT_DIRECTORY(char[] data){ create(toByteArray(data), 0); }
  public IMAGE_IMPORT_DIRECTORY(byte[] data, int offset){ create(data, offset); }
  public IMAGE_IMPORT_DIRECTORY(char[] data, int offset){ create(toByteArray(data), offset); }
  public void create(byte[] data, int offset){
    AddressOfImportLookupTable = getInt(data, offset+0x00);
    TimeDateStamp              = getInt(data, offset+0x04);
    ForwarderChain             = getInt(data, offset+0x08);
    AddressOfName              = getInt(data, offset+0x0c);
    AddressOfImportTable       = getInt(data, offset+0x10);
  }
  public void print(PrintStream out){ print(out, 0); }
  public void print(PrintStream out, int indent){
    String i = "";
    for(int x = 0; x < indent; x++) i += "  ";
    out.println(i+"IMAGE_IMPORT_DIRECTORY");
    out.println(i+"  Lookup Table Address: 0x" + hex(AddressOfImportLookupTable));
    out.println(i+"  Time Date Stamp:      0x" + hex(TimeDateStamp));
    out.println(i+"  Forwarder Chain:      0x" + hex(ForwarderChain));
    out.println(i+"  Name Address:         0x" + hex(AddressOfName));
    out.println(i+"  Import Table Address: 0x" + hex(AddressOfImportTable));
    out.println(i+"END IMAGE_IMPORT_DIRECTORY");
  }
}