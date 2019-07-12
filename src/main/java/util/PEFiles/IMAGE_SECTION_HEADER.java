package util.PEFiles;
import java.io.PrintStream;

public class IMAGE_SECTION_HEADER extends HEADER{
  /*************************************************
   *#define IMAGE_SIZEOF_SHORT_NAME 8              *
   *typedef struct _IMAGE_SECTION_HEADER {         *
   *  UCHAR   Name[IMAGE_SIZEOF_SHORT_NAME]; : 0x00*
   *  union {                                      *
   *    ULONG   PhysicalAddress;                   *
   *    ULONG   VirtualSize;                       *
   *  } Misc;                                : 0x04*
   *  ULONG   VirtualAddress;                : 0x08*
   *  ULONG   SizeOfRawData;                 : 0x0c*
   *  ULONG   PointerToRawData;              : 0x10*
   *  ULONG   PointerToRelocations;          : 0x14*
   *  ULONG   PointerToLinenumbers;          : 0x18*
   *  USHORT  NumberOfRelocations;           : 0x20*
   *  USHORT  NumberOfLinenumbers;           : 0x24*
   *  ULONG   Characteristics;               : 0x28*
   *}                                              *
   *************************************************/ 
  private static final int IMAGE_SIZEOF_SHORT_NAME = 8;
  public String Name;
  public int PhysicalAddress, VirtualSize, Misc;
  public int VirtualAddress;
  public int SizeOfRawData;
  public int PointerToRawData;
  public int PointerToRelocations;
  public int PointerToLinenumbers;
  public short NumberOfRelocations;
  public short NumberOfLinenumbers;
  public int Characteristics;
  public IMAGE_SECTION_HEADER(byte[] data){ create(data, 0); }
  public IMAGE_SECTION_HEADER(char[] data){ create(toByteArray(data), 0); }
  public IMAGE_SECTION_HEADER(byte[] data, int offset){ create(data, offset); }
  public IMAGE_SECTION_HEADER(char[] data, int offset){ create(toByteArray(data), offset); }
  public void create(byte[] data, int offset){
    Name = getString(data, offset, IMAGE_SIZEOF_SHORT_NAME);
    PhysicalAddress = VirtualSize = Misc = getInt(data, offset+0x08);
    VirtualAddress                       = getInt(data, offset+0x0c);
    SizeOfRawData                        = getInt(data, offset+0x10);
    PointerToRawData                     = getInt(data, offset+0x14);
    PointerToRelocations                 = getInt(data, offset+0x18);
    PointerToLinenumbers                 = getInt(data, offset+0x1c);
    NumberOfRelocations                  = getShort(data, offset+0x20);
    NumberOfLinenumbers                  = getShort(data, offset+0x22);
    Characteristics                      = getInt(data, offset+0x24);
  }
  public void print(PrintStream out){ print(out, 0); }
  public void print(PrintStream out, int indent){
    String i = "";
    for(int x = 0; x < indent; x++) i += "  ";
    out.println(i+"IMAGE_SECTION_HEADER");
    out.println(i+"  Name:                 " + Name);
    out.println(i+"  Misc:                 0x" + hex(Misc));
    out.println(i+"  Virtual Address:      0x" + hex(VirtualAddress));
    out.println(i+"  Raw Data Size:        0x" + hex(SizeOfRawData));
    out.println(i+"  Raw Data Address:     0x" + hex(PointerToRawData));
    out.println(i+"  Relocations Address:  0x" + hex(PointerToRelocations));
    out.println(i+"  Line Numbers Address: 0x" + hex(PointerToLinenumbers));
    out.println(i+"  Relocation Count:     " + NumberOfRelocations);
    out.println(i+"  Line Number Count:    " + NumberOfLinenumbers);
    out.println(i+"  Characteristics:      0x" + hex(Characteristics));
    out.println(i+"END IMAGE_SECTION_HEADER");
  }
}