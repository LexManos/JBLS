package HTTP;
import java.io.*;
import util.*;

class HTTPSendFile extends Thread {
  private final static String CRLF = "\r\n";
  private DataOutputStream out = null;
  private String file = null;
  private int ID = 0;
  public HTTPSendFile(DataOutputStream out, String file, int ID){
    this.out = out;
    this.file = file;
    this.ID = ID;
  }
  public void run(){
        int size = 0, curPos = 0;
        try{
          FileInputStream fis = new  FileInputStream(file);
          size = fis.available();
          Out.info("FileSend: " + ID, "Attempting to send " + file);

          out.writeBytes(HTTPServer.buildHeader(200, "application/zip", size) + CRLF);
          int lastPer = -1;
          byte[] buff = new byte[1024];
		  int flag = fis.read(buff);
		  while(flag != -1) {
		    out.write(buff, 0, flag);
		    curPos += flag;
		    int curPer = (int)(((double)curPos/(double)size)*100);
		    if (curPer % 10 == 0 && curPer > lastPer){
		      Out.info("FileSend: " + ID, curPer + "% compleet");
		      lastPer = curPer;
		    }
		    flag = fis.read(buff);
		  }
          Out.info("FileSend: " + ID, "Sending " + file + " complete");
          fis.close();
          fis = null;
        }catch(IOException e){
          Out.info("FileSend: " + ID, "Sending " + file + " failed client canceled downlod");
          return;
        }
  }
}
