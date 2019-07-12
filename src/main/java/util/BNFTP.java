package util;

import java.net.*;
import java.io.*;
public class BNFTP extends Thread{
  private Socket bnSCK = null;
  private OutputStream out = null;
  private InputStream in = null;

  private String server = null;
  private int port = 6112;
  private String file = null;
  private BotNet bn = null;
  private int id = 0;
  
  public static void main(String[] args) {}
  
  public BNFTP(String bnServer, int bnPort, String fileName){
    this.server = bnServer;
    this.port = bnPort;
    this.file = fileName;
  }
  public BNFTP(String bnServer, int bnPort, String fileName, BotNet callback, int userid){
    this.server = bnServer;
    this.port = bnPort;
    this.file = fileName;
    this.bn = callback;
    this.id = userid;
  }
  public void run(){ download(server, port, file, bn, id); }
  public String download(String bnServer, int bnPort, String fileName, BotNet callback, int userid){
    try {
      bnSCK = new Socket(bnServer, bnPort);

      out = bnSCK.getOutputStream();
      in = bnSCK.getInputStream();


      out.write((byte)0x02);

      Buffer pRequest = new Buffer();
      pRequest.add((short)(fileName.length() + 0x21));
      pRequest.add((short)0x0100);    //Version
      pRequest.add(0x49583836);       //IX86
      pRequest.add(0x44324456);       //D2DV
      pRequest.add(0x00);             //Banner ID
      pRequest.add(0x00);             //Banner Ext
      pRequest.add(0x00);             //FileTime HighDword
      pRequest.add(0x00);             //FileTime LowDword
      pRequest.add(0x00);             //Starting
      pRequest.addNTString(fileName); //File name
      Out.println("BNFTP", "Sending request for " + fileName);
      if(callback != null) callback.whisper(userid, "BNFTP: Sending request for " + fileName);
      out.write(pRequest.getBuffer());

      try {
        short headlen;
        int i;
        i = in.read();
        if (i == -1)
          throw new IOException("File not found on Server");
        headlen = (short) ((i << 0) & 0x000000FF);

        i = in.read();
        if (i == -1)
          throw new IOException("File not found on Server");
        headlen |= (short) ((i << 8) & 0x0000FF00);

        Buffer header = new Buffer();

        int bytesRead = 2;
        while (bytesRead < headlen){
           i = in.read();
          if (i == -1)
            throw new IOException("Connection terminated. " + bytesRead);
          header.add((char)i);
          bytesRead++;
        }

        header.removeWord();
        int fileSize = header.removeDWord();
        header.removeDWord(); //banner ID
        header.removeDWord(); //banner extention
        long filetime = header.removeLong();
        header.removeNTString(); //Filename
        Out.println("BNFTP", "Recived header: " );
        Out.println("BNFTP", "FileSize: " + fileSize);
        if(callback != null) callback.whisper(userid, "BNFTP: " + fileName + " Recived File Size " + fileSize);
          FileOutputStream file = new FileOutputStream(Constants.DownloadPath + fileName);

        bytesRead = 0;
        while (bytesRead < fileSize) {
          i = in.read();
          if (i == -1)
            throw new IOException("Failed to recive all file data. Last offset: " + bytesRead);
          file.write((byte)i);
          bytesRead++;
        }
        file.flush();
        file.close();
        new File(Constants.DownloadPath + fileName).setLastModified(fileTimeToMillis(filetime));
        Out.println("BNFTP", "Recived file: " + fileName + " Size: " + fileSize);
        if(callback != null) callback.whisper(userid, "BNFTP: Recived file: " + fileName + " Size: " + fileSize);
        return "Recived file: " + fileName + " Size: " + fileSize;
      }catch (IOException e) {
        Out.println("BNFTP", "IOError: " + e.toString());
        if(callback != null) callback.whisper(userid, "BNFTP: " + fileName + " IOError: " + e.toString());
        return ("IOError: " + e.toString());
      }

    }catch (UnknownHostException e) {
      Out.println("BNFTP", "Could not find host: " + bnServer + ":" + bnPort);
        if(callback != null) callback.whisper(userid, "BNFTP: Could not find host: " + bnServer + ":" + bnPort);
      return ("Could not find host: " + bnServer + ":" + bnPort);
    }catch (IOException e){
      Out.println("BNFTP", "IOException: " + e.toString());
      return ("IOExcaption: " + e.toString());
    }
  }
    public static long fileTimeToMillis(long fileTime){
       return (fileTime / 10000L) - 11644473600000L;
    }
}
