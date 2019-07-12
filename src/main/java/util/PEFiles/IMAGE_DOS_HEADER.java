package util.PEFiles;
import java.io.PrintStream;
public class IMAGE_DOS_HEADER extends HEADER{
  /***********************************************************************
   *typedef struct _IMAGE_DOS_HEADER {  // DOS .EXE header               *
   *  USHORT e_magic;         // Magic number                      : 0x00*
   *  USHORT e_cblp;          // Bytes on last page of file        : 0x02*
   *  USHORT e_cp;            // Pages in file                     : 0x04*
   *  USHORT e_crlc;          // Relocations                       : 0x06*
   *  USHORT e_cparhdr;       // Size of header in paragraphs      : 0x08*
   *  USHORT e_minalloc;      // Minimum extra paragraphs needed   : 0x0a*
   *  USHORT e_maxalloc;      // Maximum extra paragraphs needed   : 0x0c*
   *  USHORT e_ss;            // Initial (relative) SS value       : 0x0e*
   *  USHORT e_sp;            // Initial SP value                  : 0x10*
   *  USHORT e_csum;          // Checksum                          : 0x12*
   *  USHORT e_ip;            // Initial IP value                  : 0x14*
   *  USHORT e_cs;            // Initial (relative) CS value       : 0x16*
   *  USHORT e_lfarlc;        // File address of relocation table  : 0x18*
   *  USHORT e_ovno;          // Overlay number                    : 0x1a*
   *  USHORT e_res[4];        // Reserved words                    : 0x1c*
   *  USHORT e_oemid;         // OEM identifier (for e_oeminfo)    : 0x24*
   *  USHORT e_oeminfo;       // OEM information; e_oemid specific : 0x26*
   *  USHORT e_res2[10];      // Reserved words                    : 0x28*
   *  LONG   e_lfanew;        // File address of new exe header    : 0x3c*
   *}                                                                    *
   ***********************************************************************/
  public short e_magic;
  public short e_cblp;
  public short e_cp;
  public short e_crcl;
  public short e_cparhdr;
  public short e_minalloc;
  public short e_maxalloc;
  public short e_ss;
  public short e_sp;
  public short e_csum;
  public short e_ip;
  public short e_cs;
  public short e_lfarlc;
  public short e_ovno;
  public short[] e_res = new short[4];
  public short e_oemid;
  public short e_oeminfo;
  public short[] e_res2 = new short[10];
  public int e_lfanew;
  
  public IMAGE_DOS_HEADER(byte[] data){ create(data, 0); }
  public IMAGE_DOS_HEADER(char[] data){ create(toByteArray(data), 0); }
  public IMAGE_DOS_HEADER(byte[] data, int offset){ create(data, offset); }
  public IMAGE_DOS_HEADER(char[] data, int offset){ create(toByteArray(data), offset); }
  public void create(byte[] data, int offset){
    e_magic    = getShort(data, offset+0x00);
    e_cblp     = getShort(data, offset+0x02);
    e_cp       = getShort(data, offset+0x04);
    e_crcl     = getShort(data, offset+0x06);
    e_cparhdr  = getShort(data, offset+0x08);
    e_minalloc = getShort(data, offset+0x0a);
    e_maxalloc = getShort(data, offset+0x0c);
    e_ss       = getShort(data, offset+0x0e);
    e_sp       = getShort(data, offset+0x10);
    e_csum     = getShort(data, offset+0x12);
    e_ip       = getShort(data, offset+0x14);
    e_cs       = getShort(data, offset+0x16);
    e_lfarlc   = getShort(data, offset+0x18);
    e_ovno     = getShort(data, offset+0x1a);
    for(int x  = 0; x < 4; x++)
      e_res[x] = getShort(data, offset+0x1c+(x*2));
    e_oemid    = getShort(data, offset+0x24);
    e_oeminfo  = getShort(data, offset+0x26);
    for(int x  = 0; x < 10; x++)
      e_res2[x]= getShort(data, offset+0x28+(x*2));
    e_lfanew   = getInt(data, offset+0x3c);
  }  
  public void print(PrintStream out){ print(out, 0); }
  public void print(PrintStream out, int indent){
    String i = "";
    for(int x = 0; x < indent; x++) i += "  ";
    out.println(i+"IMAGE_DOS_HEADER");
    out.println(i+"  Magic:              0x" + hex(e_magic));
    out.println(i+"  CBLP:               0x" + hex(e_cblp));
    out.println(i+"  Pages:              " + e_cp);
    out.println(i+"  Relocations:        " + e_crcl);
    out.println(i+"  Header Size:        " + e_cparhdr);
    out.println(i+"  Min Alloc:          0x" + hex(e_minalloc));
    out.println(i+"  Max Alloc:          0x" + hex(e_maxalloc));
    out.println(i+"  SS:                 " + e_ss);
    out.println(i+"  SP:                 " + e_sp);
    out.println(i+"  Checksum:           0x" + hex(e_csum));
    out.println(i+"  IP:                 " + e_ip);
    out.println(i+"  CS:                 " + e_cs);
    out.println(i+"  Reloc Address:      0x" + hex(e_lfarlc));
    out.println(i+"  Overlay Number:     " + e_ovno);
    out.print  (i+"  Reserved1:         ");
    for(int x  = 0; x < 4; x++)
      out.print(" 0x" + hex(e_res[x]));
    out.println("");
    out.println(i+"  OEM ID:             " + e_oemid);
    out.println(i+"  OEM Info:           " + e_oeminfo);
    out.print  (i+"  Reserved2:         ");
    for(int x  = 0; x < 10; x++)
      out.print(" 0x" + hex(e_res2[x]));
    out.println("");
    out.println(i+"  New Header Address: 0x" + hex(e_lfanew));
    out.println(i+"END IMAGE_DOS_HEADER");
  }
}