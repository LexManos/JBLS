package Hashing;
import java.io.*;

import util.ByteFromIntArray;

public class lockdown_SHA1{
  private int[] bitlen = new int[2];
  private int[] state = new int[0x15];
  
  public lockdown_SHA1(){
	  bitlen[0] = 0;
	  bitlen[1] = 0;
	  state[0]  = 0x67452301;
	  state[1]  = 0xEFCDAB89;
	  state[2]  = 0x98BADCFE;
	  state[3]  = 0x10325476;
	  state[4]  = 0xC3D2E1F0;
  }
  
  public int[] digest(){
    byte[] vars = (new ByteFromIntArray(true)).getByteArray(bitlen);
    int len = (-((bitlen[0] >> 3 | bitlen[1] << 29) + 9) & 0x3f) + 1;
    char[] MysteryBuffer = new char[len];
    MysteryBuffer[0] = (char)0x80;
    for(int x = 1; x < len; x++) MysteryBuffer[x] = (char)0;
    
    update(MysteryBuffer);
    update(byteArrayToCharArray(vars));
    
    int[] hash = new int[5];
    for(int x = 0; x < 5; x++)
      hash[x] = state[x];
      
    return hash;
  }  
  public void update(char[] data){
    int a=0, b=0, c=0, x=0, len = data.length;
    c = len >> 29;
    b = len << 3;
    
    a = (bitlen[0] / 8) & 0x3F;
    
	  if(bitlen[0] + b < bitlen[0] || bitlen[0] + b < b) bitlen[1]++;
    bitlen[0] += b;
	  bitlen[1] += c;
    
    len += a;
    x = -a;
    ByteFromIntArray bfia = new ByteFromIntArray(true);
    
    if(len >= 0x40){
		  if(a>0){
			  while(a < 0x40){
          bfia.insertByte(state, a+0x14, (byte)data[a+x]);
				  a++;
			  }
        transform(state);
			  len -= 0x40;
			  x += 0x40;
			  a = 0;
		  }
		  if(len >= 0x40){
			  b = len;
			  for(int i = 0; i < b / 0x40; i++){
          //int[] buff = new int[0x15];
          for(int y = 0; y < 0x40; y++)
            bfia.insertByte(state, y+0x14, (byte)data[x+y]);
          transform(state);
				  len -= 0x40;
				  x += 0x40;
			  }
		  }
	  }
    while(a < len){
      bfia.insertByte(state, 20+a, (byte)data[a+x]);
      a++;
    }
	  return;
  }
  private static void transform(int[] hashBuffer){
    int buf[] = new int[0x50];
    int dw, a, b, c, d, e, p, i;
    for(i = 0; i < 0x10 ; i++)
      buf[i] = hashBuffer[i + 5];

	  for(i = 0; i < 0x40; i++){
      dw = buf[i+13]^buf[i+8]^buf[i+0]^buf[i+2];
      buf[i+16] = (dw >>> 0x1f) | (dw << 1);
    }

    a = hashBuffer[0];
    b = hashBuffer[1];
    c = hashBuffer[2];
    d = hashBuffer[3];
    e = hashBuffer[4];

    p = 0;

    i = 0x14;
    do{
      dw = ((a << 5) | (a >>> 0x1b)) + ((~b & d) | (c & b)) + e + buf[p++] + 0x5a827999;
      e = d; d = c; c = (b >>> 2) | (b << 0x1e); b = a; a = dw;
    }while(--i > 0);

    i = 0x14;
    do{
      dw = (d ^ c ^ b) + e + ((a << 5) | (a >>> 0x1b)) + buf[p++] + 0x6ED9EBA1;
      e = d; d = c; c = (b >>> 2) | (b << 0x1e); b = a; a = dw;
    }while(--i > 0);

    i = 0x14;
    do{
      dw = ((c & b) | (d & c) | (d & b)) + e + ((a << 5) | (a >>> 0x1b)) + buf[p++] - 0x70E44324;
      e = d; d = c; c = (b >>> 2) | (b << 0x1e); b = a; a = dw;
    }while(--i > 0);

    i = 0x14;
    do{
      dw = ((a << 5) | (a >>> 0x1b)) + e + (d ^ c ^ b) + buf[p++] - 0x359D3E2A;
      e = d; d = c; c = (b >>> 2) | (b << 0x1e); b = a; a = dw;
    }while(--i > 0);

    hashBuffer[0] += a;
    hashBuffer[1] += b;
    hashBuffer[2] += c;
    hashBuffer[3] += d;
    hashBuffer[4] += e;
  }
  public void pad(int amount){
    char[] emptybuffer = new char[0x1000];
    for(int x = 0; x < 0x1000; x++) emptybuffer[x] = (byte)0;
    while(amount > 0x1000){
      update(emptybuffer);
      amount -= 0x1000;
    }
    emptybuffer = new char[amount];
    for(int x = 0; x < amount; x++) emptybuffer[x] = (byte)0;
    update(emptybuffer);
  }  
  public boolean hash_file(String filename){
    try{
      byte[] data = new byte[(int)(new File(filename)).length()];
        InputStream in = new FileInputStream(filename);
      in.read(data);
      in.close();
      update(byteArrayToCharArray(data));
    }catch(Exception e){
      System.out.println("lockdown_SHA1.hash_file(" + filename + ") Failed: " + e.toString());
      return false;
    }
    return true;
  }
  private char[] byteArrayToCharArray(byte[] a){
    char[] buff = new char[a.length];
    for(int x = 0; x < a.length; x++)
      buff[x] = (char)a[x];
    return buff;
  }
}