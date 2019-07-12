package util.PEFiles;
import java.io.PrintStream;
public class IMAGE_OPTIONAL_HEADER32 extends HEADER{
  /********************************************************************************
   *typedef struct _IMAGE_OPTIONAL_HEADER {                                       *
   *  // Standard fields.                                                         *
   *  WORD    Magic;                                                        : 0x00*
   *  BYTE    MajorLinkerVersion;                                           : 0x02*
   *  BYTE    MinorLinkerVersion;                                           : 0x03*
   *  DWORD   SizeOfCode;                                                   : 0x04*
   *  DWORD   SizeOfInitializedData;                                        : 0x08*
   *  DWORD   SizeOfUninitializedData;                                      : 0x0c*
   *  DWORD   AddressOfEntryPoint;                                          : 0x10*
   *  DWORD   BaseOfCode;                                                   : 0x14*
   *  DWORD   BaseOfData;                                                   : 0x18*
   *  // NT additional fields.                                                    *
   *  DWORD   ImageBase;                                                    : 0x1c*
   *  DWORD   SectionAlignment;                                             : 0x20*
   *  DWORD   FileAlignment;                                                : 0x24*
   *  WORD    MajorOperatingSystemVersion;                                  : 0x28*
   *  WORD    MinorOperatingSystemVersion;                                  : 0x2a*
   *  WORD    MajorImageVersion;                                            : 0x2c*
   *  WORD    MinorImageVersion;                                            : 0x2e*
   *  WORD    MajorSubsystemVersion;                                        : 0x30*
   *  WORD    MinorSubsystemVersion;                                        : 0x32*
   *  DWORD   Win32VersionValue;                                            : 0x34*
   *  DWORD   SizeOfImage;                                                  : 0x38*
   *  DWORD   SizeOfHeaders;                                                : 0x3c*
   *  DWORD   CheckSum;                                                     : 0x40*
   *  WORD    Subsystem;                                                    : 0x44*
   *  WORD    DllCharacteristics;                                           : 0x46*
   *  DWORD   SizeOfStackReserve;                                           : 0x48*
   *  DWORD   SizeOfStackCommit;                                            : 0x4c*
   *  DWORD   SizeOfHeapReserve;                                            : 0x50*
   *  DWORD   SizeOfHeapCommit;                                             : 0x54*
   *  DWORD   LoaderFlags;                                                  : 0x58*
   *  DWORD   NumberOfRvaAndSizes;                                          : 0x5c*
   *  IMAGE_DATA_DIRECTORY DataDirectory[IMAGE_NUMBEROF_DIRECTORY_ENTRIES]; : 0x60*
   *}                                                                             *
   ********************************************************************************/
  private static final int IMAGE_NUMBEROF_DIRECTORY_ENTRIES = 16;
  public short Magic;
  public byte MajorLinkerVersion;
  public byte MinorLinkerVersion;
  public int SizeOfCode;
  public int SizeOfInitializedData;
  public int SizeOfUninitializedData;
  public int AddressOfEntryPoint;
  public int BaseOfCode;
  public int BaseOfData;
  public int ImageBase;
  public int SectionAlignment;
  public int FileAlignment;
  public short MajorOperatingSystemVersion;
  public short MinorOperatingSystemVersion;
  public short MajorImageVersion;
  public short MinorImageVersion;
  public short MajorSubsystemVersion;
  public short MinorSubsystemVersion;
  public int Win32VersionValue;
  public int SizeOfImage;
  public int SizeOfHeaders;
  public int CheckSum;
  public short Subsystem;
  public short DllCharacteristics;
  public int SizeOfStackReserve;
  public int SizeOfStackCommit;
  public int SizeOfHeapReserve;
  public int SizeOfHeapCommit;
  public int LoaderFlags;
  public int NumberOfRvaAndSizes;
  public IMAGE_DATA_DIRECTORY[] DataDirectory = new IMAGE_DATA_DIRECTORY[IMAGE_NUMBEROF_DIRECTORY_ENTRIES];
  
  public IMAGE_OPTIONAL_HEADER32(byte[] data){ create(data, 0); }
  public IMAGE_OPTIONAL_HEADER32(char[] data){ create(toByteArray(data), 0); }
  public IMAGE_OPTIONAL_HEADER32(byte[] data, int offset){ create(data, offset); }
  public IMAGE_OPTIONAL_HEADER32(char[] data, int offset){ create(toByteArray(data), offset); }
  public void create(byte[] data, int offset){  
    Magic                       = getShort(data, offset);
    MajorLinkerVersion          = data[offset+0x02];
    MinorLinkerVersion          = data[offset+0x03];
    SizeOfCode                  = getInt(data, offset+0x04);
    SizeOfInitializedData       = getInt(data, offset+0x08);
    SizeOfUninitializedData     = getInt(data, offset+0x0c);
    AddressOfEntryPoint         = getInt(data, offset+0x10);
    BaseOfCode                  = getInt(data, offset+0x14);
    BaseOfData                  = getInt(data, offset+0x18);
    ImageBase                   = getInt(data, offset+0x1c);
    SectionAlignment            = getInt(data, offset+0x20);
    FileAlignment               = getInt(data, offset+0x24);
    MajorOperatingSystemVersion = getShort(data, offset+0x28);
    MinorOperatingSystemVersion = getShort(data, offset+0x2a);
    MajorImageVersion           = getShort(data, offset+0x2c);
    MinorImageVersion           = getShort(data, offset+0x2e);
    MajorSubsystemVersion       = getShort(data, offset+0x30);
    MajorSubsystemVersion       = getShort(data, offset+0x32);
    Win32VersionValue           = getInt(data, offset+0x34);
    SizeOfImage                 = getInt(data, offset+0x38);
    SizeOfHeaders               = getInt(data, offset+0x3c);
    CheckSum                    = getInt(data, offset+0x40);
    Subsystem                   = getShort(data, offset+0x44);
    DllCharacteristics          = getShort(data, offset+0x46);
    SizeOfStackReserve          = getInt(data, offset+0x48);
    SizeOfStackCommit           = getInt(data, offset+0x4c);
    SizeOfHeapReserve           = getInt(data, offset+0x50);
    SizeOfHeapCommit            = getInt(data, offset+0x54);
    LoaderFlags                 = getInt(data, offset+0x58);
    NumberOfRvaAndSizes         = getInt(data, offset+0x5c);
    for(int x = 0; x < IMAGE_NUMBEROF_DIRECTORY_ENTRIES; x++)
      DataDirectory[x] = new IMAGE_DATA_DIRECTORY(data, offset+0x60+(x*0x08));
  }
  public void print(PrintStream out){ print(out, 0); }
  public void print(PrintStream out, int indent){
    String i = "";
    for(int x = 0; x < indent; x++) i += "  ";
    out.println(i+"IMAGE_OPTIONAL_HEADER32");
    out.println(i+"  Magic:                    0x" + hex(Magic));
    out.println(i+"  Linker Version:           " + MajorLinkerVersion + "." + MinorLinkerVersion);
    out.println(i+"  Code Size:                0x" + hex(SizeOfCode));
    out.println(i+"  Initialized Data Size:    0x" + hex(SizeOfInitializedData));
    out.println(i+"  Uninitialized Data Size:  0x" + hex(SizeOfUninitializedData));
    out.println(i+"  Entry Point:              0x" + hex(AddressOfEntryPoint));
    out.println(i+"  Code Base:                0x" + hex(BaseOfCode));
    out.println(i+"  Data Base:                0x" + hex(BaseOfData));
    out.println(i+"  Image Base:               0x" + hex(ImageBase));
    out.println(i+"  Section Alignment:        0x" + hex(SectionAlignment));
    out.println(i+"  File Alignment:           0x" + hex(FileAlignment));
    out.println(i+"  Operating System Version: " + MajorOperatingSystemVersion + "." + MinorOperatingSystemVersion);
    out.println(i+"  Image Version:            " + MajorImageVersion + "." + MinorImageVersion);
    out.println(i+"  Subsystem Version:        " + MajorSubsystemVersion + "." + MinorSubsystemVersion);
    out.println(i+"  Win32 Version Value:      " + Win32VersionValue);
    out.println(i+"  Image Size:               0x" + hex(SizeOfImage));
    out.println(i+"  Header Size:              0x" + hex(SizeOfHeaders));
    out.println(i+"  CheckSum:                 0x" + hex(CheckSum));
    out.println(i+"  Subsystem:                " + Subsystem);
    out.println(i+"  DLL Characteristics:      0x" + hex(DllCharacteristics));
    out.println(i+"  Stack Reserve Size:       0x" + hex(SizeOfStackReserve));
    out.println(i+"  Stack Commit Size:        0x" + hex(SizeOfStackCommit));
    out.println(i+"  Heap Reserve Size:        0x" + hex(SizeOfHeapReserve));
    out.println(i+"  Heap Commit Size:         0x" + hex(SizeOfHeapCommit));
    out.println(i+"  Loader Flags:             0x" + hex(LoaderFlags));
    out.println(i+"  Number of Rva and Sizes:  " + NumberOfRvaAndSizes);
    for(int x = 0; x < DataDirectory.length; x++)
      DataDirectory[x].print(out, indent+1);
    out.println(i+"END IMAGE_OPTIONAL_HEADER32");
  }
}