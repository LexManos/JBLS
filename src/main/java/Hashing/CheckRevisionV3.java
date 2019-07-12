package Hashing;
import java.io.*;
import java.util.*;

import util.*;
import util.PEFiles.*;
import Hashing.*;

public class CheckRevisionV3 extends CheckRevisionV1{
  private static int Version[][] = new int[3][0x0C];
  private static int seeds[][] = new int[20][2];
  private static Hashtable<String, lockdown_heap> heaps = new Hashtable<String, lockdown_heap>();
  private static Hashtable<String, PEFile> pes = new Hashtable<String, PEFile>();
  private static Hashtable<String, CheckrevisionResults> crCache = new Hashtable<String, CheckrevisionResults>();
  private static int crCacheHits = 0;
  private static int crCacheMisses = 0;
  
  
  public static void clearCache(){
    Version = new int[3][0x0c];
    crCacheHits = 0;
    crCacheMisses = 0;
    crCache = new Hashtable<String, CheckrevisionResults>();
    heaps = new Hashtable<String, lockdown_heap>();
    pes = new Hashtable<String, PEFile>();
    System.gc();
  }
  
  public static long word_shifter(int str1, int str2){
    str2 = (short)((((str1 >> 8) + (str1 & 0xFF)) >> 8) + (((str1 >> 8) + (str1 & 0xFF)) & 0xFF));
    str2 = (short)((str2 & 0xFF00) | (((str2+1) & 0xFF) - (((str2 & 0xFF) != 0xFF) ? 1 : 0)));

    str1 = (short)(((str1 - str2) & 0xFF) | (((((str1 - str2) >> 8) & 0xFF)+1) > 0 ? 0 : 0x100));
    str1 = (short)((str1 & 0xFF00) | (-str1 & 0xFF));
    
    long ret = str1 | (str2 << 16);
    return ret;
  }
  public static char[] shuffle_value_string(String value_string){
    int pos = 0;
    char shifter, adder;
    char[] str = value_string.toCharArray();
    char[] buffer = new char[0x10];
    
    for(int x = str.length; x > 0; x--){
      shifter = 0;
      for(int i = 0; i < pos; i++){
        char b = buffer[i];
        buffer[i] = (char)((-buffer[i] + shifter) & 0xFF);
        shifter = (char)(((((b << 8) - b) + shifter) >> 8) & 0xFF);
      }
      if(shifter > 0){
        if(pos >= 0x10) return null;
        buffer[pos++] = shifter;
      }
      adder = (char)(str[x - 1] - 1);
      for(int i = 0; i < pos && adder>0; i++){
        buffer[i] = (char)((buffer[i] + adder)&0xFF);
        adder = (char)(buffer[i] < adder ? 1 : 0);
      }
      if(adder>0){
        if(pos >= 0x10) return null;
        buffer[pos++] = adder;
      }
    }
    while(pos < 0x10) buffer[pos++] = (char)0;
    return buffer;
  }
 
 public static char[] digest_shuffle(char[] str2){
    int word1, word2, x=0;
    char[] buff = new char[0x100];
    for(int i = 0x10; i > 0; ){
      while(i>0 && str2[i-1] == (char)0) i--;
      
      if(i > 0){
        word1 = 0;
        for(int j = i - 1; j >= 0; j--){
          word2 = (word1 << 8) + ((int)str2[j] & 0xFF);
          long r = word_shifter(word2, word1);
          word2 = (int)(r & 0xFFFF);
          word1 = (int)((r & 0xFFFF0000) >> 16);
          str2[j] = (char)word2;
        }
        if((0x10 - i) >= 0xff)
          return null;
        else
          buff[x++] = (char)(word1 + 1);
      }
    }
    char[] ret = new char[x];
    System.arraycopy(buff, 0, ret, 0, x);
  
    return ret;
  }
  public static CheckrevisionResults checkRevision(String valuestring, int prod, byte plat, String mpq) throws FileNotFoundException, IOException{
    CheckrevisionResults cacheHit = (CheckrevisionResults)crCache.get(valuestring + mpq + prod + plat);
    if(cacheHit != null){
      Out.println("CREV", "CheckRevision cache hit: " + crCacheHits + " hits, " + crCacheMisses + " misses.");
      crCacheHits++;
      return cacheHit;
    }
    Out.println("CREV", "CheckRevision cache miss: " + crCacheHits + " hits, " + crCacheMisses + " misses.");
    crCacheMisses++;
    
    lockdown_SHA1 ctx = new lockdown_SHA1();
    char[] return_is_valid = new char[]{(char)1, (char)0, (char)0, (char)0};
    char[] module_offset   = new char[]{(char)0, (char)0, (char)0, (char)0};
    int[] hashbuff = new int[4];
    int[] ld_sha1_out_buffer_1 = new int[5];
    int[] ld_sha1_out_buffer_2 = new int[5];
    char[] valuestring_encoded  = new char[0x10];
    char[] valuestring_buffer_1 = new char[0x40];
    char[] valuestring_buffer_2 = new char[0x40];
    char[] temp_memory = new char[0x10];
    mpq = mpq.replace(".mpq", ".dll");
    
    String[] files = getFiles(prod, plat);
    if(files == null) return null;
    files[4] = Constants.ArchivePath + mpq;

    valuestring_encoded = shuffle_value_string(valuestring);
    
    for(int x = 0; x < 0x40; x++){
      valuestring_buffer_1[x] = (char)'6';
      valuestring_buffer_2[x] = (char)'\\';
    }
    for(int i = 0; i < 0x10; i++){
      valuestring_buffer_1[i] ^= valuestring_encoded[i];
      valuestring_buffer_2[i] ^= valuestring_encoded[i];
    }
    ctx.update(valuestring_buffer_1);
    
    hash_file(ctx, files[4], files[4]);
    hash_file(ctx, files[0], files[4]);
    hash_file(ctx, files[1], files[4]);
    hash_file(ctx, files[2], files[4]);
    ctx.hash_file(files[3]);
    ctx.update(return_is_valid); /* Used to verify return address */
    ctx.update(module_offset);   /* Used to verify the module */
    ld_sha1_out_buffer_1 = ctx.digest();
    
    ctx = new lockdown_SHA1();
    ctx.update(valuestring_buffer_2);
    ctx.update(getCharArray(ld_sha1_out_buffer_1));
    ld_sha1_out_buffer_2 = ctx.digest();

    System.arraycopy(ld_sha1_out_buffer_2, 1, hashbuff, 0, 4);
    temp_memory = digest_shuffle(getCharArray(hashbuff));
  
    Buffer returnbuff = new Buffer();
    for(int x = 0; x < temp_memory.length; x++) returnbuff.add((byte)temp_memory[x]);
    returnbuff.addByte((byte)0);    
    if(Version[plat][prod] == 0) Version[plat][prod] = getVersion(files, prod);

    CheckrevisionResults result = new CheckrevisionResults(Version[plat][prod], ld_sha1_out_buffer_2[0], returnbuff);
    crCache.put(valuestring + mpq.replaceAll("dll", "mpq") + prod + plat, result);
    return result;
  }
  public static void hash_file(lockdown_SHA1 ctx, String filename, String lockdown) throws FileNotFoundException, IOException{
    //System.out.println("Hash File: " + filename);
    int PESectionAlignment, PEImageBase, PEImageBaseUpper;
    int ecx;
    boolean is32bit = false;
    lockdown_heap ld_heap = heaps.get(filename);
    PEFile pe = pes.get(filename);
    if(pe == null){
      pe = new PEFile(filename);
      //  pe.fillIAT();
      pes.put(filename, pe);
    }
    if(!pe.loaded) throw new FileNotFoundException(filename);
    if(pe.ntheader.Signature != PEFile.IMAGE_NT_SIGNATURE) return; //Not a valid PE file
    if(pe.ntheader.FileHeader.Characteristics < 0x00E0) return; //0.o Wtf is flag 0x40?
    if(pe.ntheader.OptionalHeader.Magic == PEFile.IMAGE_NT_OPTIONAL_HDR32_MAGIC){
      if(pe.ntheader.OptionalHeader.NumberOfRvaAndSizes <= 0x0D) return;
      PESectionAlignment = pe.ntheader.OptionalHeader.SectionAlignment;
      PEImageBase = pe.ntheader.OptionalHeader.ImageBase;
      PEImageBaseUpper = 0;
      is32bit = true;
    }else{
      return;
    }
    ecx = pe.ntheader.OptionalHeader.SizeOfHeaders;
    ecx = (ecx+pe.ntheader.OptionalHeader.FileAlignment-1) & ~(pe.ntheader.OptionalHeader.FileAlignment - 1);
    ctx.update(pe.getData(0, ecx));
    
    if(ld_heap == null){
      ld_heap = new lockdown_heap(true);
      if(pe.ntheader.OptionalHeader.DataDirectory[PEFile.RELOC_TABLE].VirtualAddress > 0)
        process_reloc(ld_heap, pe, is32bit);
      process_import(ld_heap, pe, is32bit);
//      ld_heap.sort();
      ld_heap.resize();
      heaps.put(filename, ld_heap);
    }
//    ld_heap.print();
    for(int i = 0; i < pe.sectionHeaders.length; i++)
      hash1(ctx, ld_heap, pe, PEImageBase, PEImageBaseUpper, pe.sectionHeaders[i], PESectionAlignment, lockdown);
    //ld_heap.destroy();
  }
  public static void hash1(lockdown_SHA1 ctx, lockdown_heap ld_heap, PEFile pe, int preferred_baseaddr, int preferred_baseaddr_upper, IMAGE_SECTION_HEADER section, int section_alignment, String lockdown){
    int eax, edi, i, dwBytes, index;
    int[] var_30 = new int[4];
    int[] var_40 = new int[4];
    int[] lockdown_memory = ld_heap.toIntArray();
    char[] allocated_memory_base;
      
    //System.out.println("Hash1: " + section.Name);
    edi = section.VirtualAddress;
    dwBytes = ((section.VirtualSize + section_alignment - 1) & ~(section_alignment - 1)) - section.VirtualSize;
    if(section.Characteristics<0){
      ctx.pad(dwBytes+section.VirtualSize);
    }else{
      index=0;
      if(ld_heap.length() > 0)
        for(i = 0; index < ld_heap.length() && lockdown_memory[i] < edi; i+=4)
          index++;
      if(section.VirtualSize > 0){
        int starting_memory = edi;
        int ptr_memory = edi;
        i=0;
        int memory_offset = index*4;
        while((ptr_memory - starting_memory) < section.VirtualSize){
          int section_length = starting_memory - ptr_memory + section.VirtualSize;
          eax = 0;
          if(index < ld_heap.length())
            eax = (int)(lockdown_memory[memory_offset] + starting_memory - section.VirtualAddress);
//          System.out.println(Integer.toHexString(eax));
          if(eax > 0){
            eax -= ptr_memory;
//              System.out.println(Integer.toHexString(eax));
            if(eax < section_length)
              section_length = eax;
          }
          if(section_length > 0){
//            System.out.println("Section_length_update: " + PadString.padHex(ptr_memory, 8) + " " + PadString.padHex(section_length, 8));
//            utile.print_hash(pe.getDataSection(ptr_memory, section_length));
            ctx.update(pe.getDataSection(ptr_memory, section_length));
            ptr_memory += section_length;
          }else{
            int[] heap_buffer = new int[4];
//            System.out.println("Heap Size: " + Integer.toHexString(lockdown_memory.length) + " " + Integer.toHexString(memory_offset));
//            System.out.println(Integer.toHexString(lockdown_memory.length) + " " + Integer.toHexString(memory_offset));
            System.arraycopy(lockdown_memory, memory_offset, heap_buffer, 0, 4);
            hash2(pe, preferred_baseaddr, preferred_baseaddr_upper, ctx, heap_buffer, ptr_memory, lockdown);
            ptr_memory += heap_buffer[1];
            index++;
            memory_offset += 4;
          }
        }      
      }
      if(dwBytes <= 0) return;
      allocated_memory_base = new char[dwBytes];
      if(dwBytes > 0){
        i = 0;
        while(i < dwBytes){
          eax = 0;
          if(index < ld_heap.length()){
            System.arraycopy(lockdown_memory, index*4, var_40, 0, 4);
            eax = (int)(var_40[0]-section.VirtualSize - section.VirtualAddress+allocated_memory_base[0]);
          }
          dwBytes += i;
          if(eax>0){
            eax -= pe.getInt(allocated_memory_base, i);
            if(eax < dwBytes) dwBytes = eax;
          }
          if(dwBytes>0){
            char[] mem = new char[dwBytes];
            System.arraycopy(allocated_memory_base, i, mem, 0, dwBytes);
            ctx.update(mem);
            i+=dwBytes;
          }else{
            System.arraycopy(lockdown_memory, index*4, var_30, 0, 4);
            hash2(pe, preferred_baseaddr, preferred_baseaddr_upper, ctx, var_30, allocated_memory_base[i], lockdown);
            index++;
            i+=var_30[4];
          }
        }
      }
      allocated_memory_base = null;
      System.gc();
    }
  }
  private static void hash2(PEFile pe, int preferred_baseaddr, int preferred_baseaddr_upper, lockdown_SHA1 ctx, int[] offset_memory, int ptr_memory, String lockdown){
    int cf = 1;
    //int lower_offset = -preferred_baseaddr;
    //int upper_offset = -(preferred_baseaddr_upper + cf);
    int seedIndex = Integer.parseInt(lockdown.substring(lockdown.toLowerCase().indexOf("lockdown-ix86-")).substring(14, 16));
    
    if(seeds[seedIndex][0] == 0 || seeds[seedIndex][1] == 0)
      seeds[seedIndex] = seed_finder.find_seeds(lockdown, seed_finder.lockdown_values, seed_finder.lockdown_offsets);      

    if(offset_memory[2] == 0){
      if(offset_memory[3] == 0){
        ctx.pad(offset_memory[1]);
//        System.out.println("Padd: " + Integer.toHexString(offset_memory[1]));
      }else{
//        System.out.print("0: ");utile.print_hash(pe.getDataSection(offset_memory[3], offset_memory[1]));
        ctx.update(pe.getDataSection(offset_memory[3], offset_memory[1]));
      }
    }else if(offset_memory[2] == 1){
      char[] buffer = new char[0x14];
      if(ptr_memory != 0)
        buffer = pe.getDataSection(ptr_memory, 0x14);
//      System.out.print("1: ");utile.print_hash(buffer);
      ctx.update(buffer);
    }else if(offset_memory[2] == 2){
      if(offset_memory[3] == 3){ //32-bit
        int value = 0;
        if(ptr_memory != 0) value = pe.getIntSection(ptr_memory)^seeds[seedIndex][0];
        ctx.update(getCharArray(value));
//        System.out.print("2: ");utile.print_hash(getCharArray(value));
      }
    }
  }  
  public static void process_reloc(lockdown_heap ld_heap, PEFile pe, boolean is32bit){
    int RelocSectionStart = pe.ntheader.OptionalHeader.DataDirectory[PEFile.RELOC_TABLE].VirtualAddress;
    int RelocSectionEnd = RelocSectionStart + pe.ntheader.OptionalHeader.DataDirectory[PEFile.RELOC_TABLE].Size;
    short edi; int edx, eax;

    if(RelocSectionEnd <= RelocSectionStart) return;
    //System.out.println("Start: " + Integer.toHexString(RelocSectionStart) + " End: " + Integer.toHexString(RelocSectionEnd));
    int[] DataArray = new int[4];
    while(RelocSectionStart < RelocSectionEnd){
      int var_8 = (pe.getIntSection(RelocSectionStart+4) - 8) / 2;
      if(var_8 <= 0) return;
      
      edi = pe.getShortSection(RelocSectionStart+8);
      int ep=0;
      
      while(var_8>0){
        eax = edi / 0x1000;
        if(eax < 0) return;
      
        if(eax != 0){
          if(eax == 0x0a)
            edx = 8;
          else if(eax == 3)
            edx = 4;
          else if(eax == 2)
            edx = 2;
          else
            return;
            
          DataArray[0] = pe.getIntSection(RelocSectionStart) + (edi & 0x0FFF);
          DataArray[1] = edx;
          DataArray[2] = 2;
          DataArray[3] = eax;
          ld_heap.add(getCharArray(DataArray));
        }
        ep+=2;
        edi = pe.getShortSection(RelocSectionStart+8+ep);
        var_8--;
      }
      RelocSectionStart += 8 + ep;
    }
  }
  public static void process_import(lockdown_heap ld_heap, PEFile pe, boolean is32bit){
    int importaddress = pe.ntheader.OptionalHeader.DataDirectory[PEFile.IMPORT_TABLE].VirtualAddress;
    int importsize = pe.ntheader.OptionalHeader.DataDirectory[PEFile.IMPORT_TABLE].Size;
    int[] DataArray = new int[4];
    int var_8;
    int eax=0, esi, ecx, edx;
    
    if(importaddress <= 0 || importsize < 0x14) return;
    var_8 = 8-importaddress;
    esi = importaddress+0x10;
    while(eax <= importsize){
      if(pe.getIntSection(esi-4) == 0) return;
      edx = pe.getIntSection(esi);
      if(edx == 0) return;
      eax = 0;
      ecx = edx + eax;
      if(is32bit){
        eax = ecx;
        while(pe.getIntSection(eax) != 0)
          eax += 4;
        eax = (((eax-ecx)/4)*4)+4;
      }else
        return;
        
      DataArray[0] = edx;
      DataArray[1] = eax;
      DataArray[2] = 0;
      DataArray[3] = pe.getIntSection(esi-0x10);
      //utile.print_hash(DataArray);
      ld_heap.add(getCharArray(DataArray));
      
      DataArray[0] = importaddress;
      DataArray[1] = 0x14;
      DataArray[2] = 1;
      DataArray[3] = 0;
      ld_heap.add(getCharArray(DataArray));
      
      eax = var_8;
      importaddress += 0x14;
      esi += 0x14;
      eax += esi - 4;
    }
  }
  
  public static String[] getFiles(int prod, byte plat){
    String[] ret = {"", "", "", "", ""};
    if(prod < 0) return null;
    if(prod > Constants.prods.length + 1) return null;
    switch(plat){
      case Constants.PLATFORM_INTEL:
        ret[0] = Constants.IX86files[prod-1][0] + Constants.IX86files[prod-1][1];
        ret[1] = Constants.IX86files[prod-1][0] + Constants.IX86files[prod-1][2];
        ret[2] = Constants.IX86files[prod-1][0] + Constants.IX86files[prod-1][3]; 
        ret[3] = Constants.IX86files[prod-1][0] + Constants.IX86files[prod-1][4]; //Screen dump
        break;
      default: ret = null;
    }
    return ret;
  }
  private static char[] getCharArray(int array){ return getCharArray(new int[]{array}); }
  private static char[] getCharArray(int[] array){
    char[] newArray = new char[array.length * 4];
    int pos = 0;
    for(int i = 0; i < array.length; i++){
      newArray[pos++] = (char)((array[i] >> 0) & 0xFF);
      newArray[pos++] = (char)((array[i] >> 8) & 0xFF);
      newArray[pos++] = (char)((array[i] >> 16) & 0xFF);
      newArray[pos++] = (char)((array[i] >> 24) & 0xFF);
    }
    return newArray;
  }
}