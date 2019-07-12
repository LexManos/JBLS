package util.PEFiles;
import java.io.PrintStream;

public class IMAGE_FILE_HEADER extends HEADER{
  /****************************************
   *typedef struct _IMAGE_FILE_HEADER {   *
   *  USHORT  Machine;              : 0x00*
   *  USHORT  NumberOfSections;     : 0x02*
   *  ULONG   TimeDateStamp;        : 0x04*
   *  ULONG   PointerToSymbolTable; : 0x08*
   *  ULONG   NumberOfSymbols;      : 0x0c*
   *  USHORT  SizeOfOptionalHeader; : 0x10*
   *  USHORT  Characteristics;      : 0x12*
   *}                                     *
   ****************************************/
  public short Machine;
  public short NumberOfSections;
  public int TimeDateStamp;
  public int PointerToSymbolTable;
  public int NumberOfSymbols;
  public short SizeOfOptionalHeader;
  public short Characteristics;
  
  public IMAGE_FILE_HEADER(byte[] data){ create(data, 0); }
  public IMAGE_FILE_HEADER(char[] data){ create(toByteArray(data), 0); }
  public IMAGE_FILE_HEADER(byte[] data, int offset){ create(data, offset); }
  public IMAGE_FILE_HEADER(char[] data, int offset){ create(toByteArray(data), offset); }
  public void create(byte[] data, int offset){
    Machine              = getShort(data, offset+0x00);
    NumberOfSections     = getShort(data, offset+0x02);
    TimeDateStamp        = getInt(data, offset+0x04);
    PointerToSymbolTable = getInt(data, offset+0x08);
    NumberOfSymbols      = getInt(data, offset+0x0c);
    SizeOfOptionalHeader = getShort(data, offset+0x10);
    Characteristics      = getShort(data, offset+0x12);
  }
  public void print(PrintStream out){ print(out, 0); }
  public void print(PrintStream out, int indent){
    String i = "";
    for(int x = 0; x < indent; x++) i += "  ";
    out.println(i+"IMAGE_FILE_HEADERS");
    out.println(i+"  Machine:              0x" + hex(Machine));
    out.println(i+"  Section Count:        " + NumberOfSections);
    out.println(i+"  Time Date Stamp:      0x" + hex(TimeDateStamp));
    out.println(i+"  Symbol Table Address: 0x" + hex(PointerToSymbolTable));
    out.println(i+"  Symbol Count:         " + NumberOfSymbols);
    out.println(i+"  Optional Header Size: 0x" + hex(SizeOfOptionalHeader));
    out.println(i+"  Characteristics:      0x" + hex(Characteristics));
    out.println(i+"END IMAGE_FILE_HEADERS");
  }
} 