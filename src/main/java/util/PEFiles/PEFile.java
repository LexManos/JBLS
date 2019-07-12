package util.PEFiles;
import java.io.*;

public class PEFile{  
  public static final int SIZE_OF_NT_SIGNATURE     = 4;
  public static final int IMAGE_SIZEOF_FILE_HEADER = 20;
  public static final int IMAGE_SIZEOF_SECTION_HEADER = 40;
  public static final int IMAGE_SIZEOF_IMPORT_HEADER = 20;
  public static final int IMAGE_DOS_SIGNATURE    = 0x5A4D;     // MZ
  public static final int IMAGE_OS2_SIGNATURE    = 0x454E;     // NE
  public static final int IMAGE_OS2_SIGNATURE_LE = 0x454C;     // LE
  public static final int IMAGE_NT_SIGNATURE     = 0x00004550; // PE00
  public static final int IMAGE_NT_OPTIONAL_HDR32_MAGIC = 0x10b; //32-bit
  public static final int IMAGE_NT_OPTIONAL_HDR64_MAGIC = 0x20b; //64-bit
  public static final int EXPORT_TABLE      = 0;
  public static final int IMPORT_TABLE      = 1;
  public static final int RESOURCE_TABLE    = 2;
  public static final int EXCEPTION_TABLE   = 3;
  public static final int CERTIFICATE_TABLE = 4;
  public static final int RELOC_TABLE       = 5;
  public static final int DEBUG_DATA        = 6;
  public static final int ARCH_DATA         = 7;
  public static final int MACHINE_VALUE     = 8;
  public static final int TLS_TABLE         = 9;
  public static final int CONFIG_TABLE      = 10;
  public static final int BIND_TABLE        = 11;
  public static final int ADDRESS_TABLE     = 12;
  public static final int DELAY_TABLE       = 13;
  public static final int COM_HEADER        = 14;
  
  String filename = null;
  byte[] data = null;
  char[] sections = null;
  int sectionsOffset = 0;
  public boolean loaded = false;
  public IMAGE_DOS_HEADER dosheader = null;
  public IMAGE_NT_HEADERS ntheader = null;
  public IMAGE_SECTION_HEADER[] sectionHeaders = null;
  
  public void fillIAT(){ fillIAT(ntheader.OptionalHeader.DataDirectory[IMPORT_TABLE]); }  
  private void fillIAT(IMAGE_DATA_DIRECTORY dir){
    if(dir.VirtualAddress == 0) return;
    dir.print(System.out, 0);
    IMAGE_IMPORT_DIRECTORY iid = new IMAGE_IMPORT_DIRECTORY(sections, dir.VirtualAddress);
    int x = 1;
    while(iid.AddressOfName != 0){
      iid.print(System.out, 0);
      System.out.println(getStringSection(iid.AddressOfName));
      
      String library = getStringSection(iid.AddressOfName);
      library = library.substring(0, library.indexOf("."));
      int y = 0;
      while(getIntSection(iid.AddressOfImportLookupTable+(4*y)) != 0){
        int fp = getIntSection(iid.AddressOfImportLookupTable+(4*y));
        //System.out.println(fp);
        int address=0;
        if(fp > sections.length || fp < 0)
          address = fp;
        else{
          String function = getStringSection(fp + 2);
          address = ImportAddressManager.getAddress(library + "." + function);
        }
        
        //System.out.println(Integer.toHexString(address) + ": " + function + ": " + Integer.toHexString(getIntSection(iid.AddressOfImportTable+(4*y))));
        if(address != 0)
          putSection(address, iid.AddressOfImportTable+(4*y));
        y++;
      }
      
      iid = new IMAGE_IMPORT_DIRECTORY(sections, dir.VirtualAddress+(x*IMAGE_SIZEOF_IMPORT_HEADER));
      x++;
    }
    System.out.println(getStringSection(getIntSection(iid.AddressOfImportTable)));
  }
  
  private void loadSection(IMAGE_SECTION_HEADER section, char[] buffer){
    char[] data  = getData(section.PointerToRawData, (section.SizeOfRawData > section.VirtualSize ? section.VirtualSize : section.SizeOfRawData));
    System.arraycopy(data, 0, buffer, section.VirtualAddress, data.length);
    for(int x = data.length; x < section.VirtualSize; x++)
      buffer[section.VirtualAddress+x] = (byte)0;
  }
  
  public PEFile(String fn){
    this.filename = fn;
    data = readFile(filename);
    
    if(data == null) return;    
    dosheader = new IMAGE_DOS_HEADER(data);
    ntheader = new IMAGE_NT_HEADERS(data, dosheader.e_lfanew);
    //dosheader.print(System.out);
    //ntheader.print(System.out);
    
    int sectionStart = ntheader.FileHeader.SizeOfOptionalHeader + dosheader.e_lfanew + SIZE_OF_NT_SIGNATURE + IMAGE_SIZEOF_FILE_HEADER;
    sectionHeaders = new IMAGE_SECTION_HEADER[ntheader.FileHeader.NumberOfSections];
    
    for(int x = 0; x < ntheader.FileHeader.NumberOfSections; x++){
      sectionHeaders[x] = new IMAGE_SECTION_HEADER(data, sectionStart+(IMAGE_SIZEOF_SECTION_HEADER*x));
      //sectionHeaders[x].print(System.out);
    }
    int sectionsSize = sectionHeaders[sectionHeaders.length-1].VirtualAddress +
                       sectionHeaders[sectionHeaders.length-1].VirtualSize;
    sectionsSize += (sectionsSize % ntheader.OptionalHeader.SectionAlignment);
    sections = new char[sectionsSize];
    for(int x = 0; x < ntheader.FileHeader.NumberOfSections; x++)
      loadSection(sectionHeaders[x], sections);
    //fillIAT(ntheader.OptionalHeader.DataDirectory[IMPORT_TABLE]);
    loaded=true;
  }
    public void putSection(int data, int offset){
    sections[offset+0] = (char)((data & 0x000000FF)>>0);
    sections[offset+1] = (char)((data & 0x0000FF00)>>8);
    sections[offset+2] = (char)((data & 0x00FF0000)>>16);
    sections[offset+3] = (char)((data & 0xFF000000)>>24);
    //System.out.println("Changing: 0x" + Integer.toHexString(offset) + " to 0x" + Integer.toHexString(data));
  }
  public char[] getSectionsData(){ return sections; }
  public byte[] getData(){ return data; }
  public char[] getData(int start, int length){
    char[] buff = new char[length];
    for(int x = 0; x < length; x++)
      buff[x] = (char)data[start+x];
    return buff;
  }
  public char[] getDataSection(int start, int length){
    char[] buff = new char[length];
    for(int x = 0; x < length; x++)
      buff[x] = (char)sections[start+x];
    return buff;
  }
  public String getStringSection(int o){ return getString(sections, o); }
  public String getString(byte[] d, int o){
    int start = o;
    while(d[o] != 0 && o < d.length) o++;
    byte[] str = new byte[o-start];
    System.arraycopy(d, start, str, 0, o-start);
    return new String(str);
  }
  public String getString(char[] d, int o){
    int start = o;
    while(d[o] != (char)0 && o < d.length) o++;
    char[] str = new char[o-start];
    System.arraycopy(d, start, str, 0, o-start);
    return new String(str);
  }
  public int getIntSection(int o){ return getInt(sections, o); }
  public int getInt(int o){ return getInt(data, o); }
  public int getInt(byte[] d, int o){
    int sig = d[o]        & 0x000000FF;
    sig |= (d[o+1] << 8)  & 0x0000FF00;
    sig |= (d[o+2] << 16) & 0x00FF0000;
    sig |= (d[o+3] << 24) & 0xFF000000;
    return sig;
  }
  public int getInt(char[] d, int o){
    int sig = (int)d[o]        & 0x000000FF;
    sig |= ((int)d[o+1] << 8)  & 0x0000FF00;
    sig |= ((int)d[o+2] << 16) & 0x00FF0000;
    sig |= ((int)d[o+3] << 24) & 0xFF000000;
    return sig;
  }
  public short getShortSection(int o){ return getShort(sections, o); }
  public short getShort(int o){ return getShort(data, o); }
  public short getShort(byte[] d, int o){
    int sig = d[o]        & 0x00FF;
    sig |= (d[o+1] << 8)  & 0xFF00;
    return (short)(sig & 0xFFFF);
  }
  public short getShort(char[] d, int o){
    int sig = d[o]        & 0x00FF;
    sig |= (d[o+1] << 8)  & 0xFF00;
    return (short)(sig & 0xFFFF);
  }
  private byte[] readFile(String file){
    try{
      byte[] ret = new byte[(int)(new File(file)).length()];
      InputStream in = new FileInputStream(file);
      in.read(ret);
      in.close();
      return ret;
    }catch(Exception e){ return null; }
  }
}