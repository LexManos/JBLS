package Hashing;

import util.Buffer;

public class CheckrevisionResults{

  private int    version  = 0x00;
  private int    checksum = 0x00;
  private Buffer info     = null;


  public CheckrevisionResults(int ver, int check, Buffer exe){
    this.version  = ver;
    this.checksum = check;
    this.info     = exe;
  }
  
  public int getVersion(){ return this.version; }
  public int getChecksum(){ return this.checksum; }
  public Buffer getInfo(){ return this.info; }
}