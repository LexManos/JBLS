package HTTP;

import java.io.*;
import java.net.*;
import java.util.*;
import util.*;
import Hashing.HashMain;
import BNLSProtocol.BNLSConnectionThread;

public class HTTPParse extends Thread{
  private Socket socket = null;
  private static int threadCount=0;
  private int threadID;
  DataOutputStream out = null;
  BufferedReader in = null;
  final static String CRLF = "\r\n";
  private String defaultCSS =
    "body{" + CRLF +
	"  background-color: #000000;" + CRLF +
	"  background-repeat: repeat;" + CRLF +
	"  text-align: center;" + CRLF +
	"  font-family: \"Arial\";" + CRLF +
	"  font-size: 12px;" + CRLF +
	"  color: #ffffff;" + CRLF +
    "}" + CRLF +
    "span.mainheader{font-size: 20px;}" + CRLF +
    ".header {font-weight: bold;}" + CRLF +
    "span.mainsub {font-style: italic;}" + CRLF +
    "span.subheader {font-weight: bold;}" + CRLF +
    ".indent {padding-left: 50pt;}" + CRLF +
    ".line {" + CRLF +
	"  font-size: 12px;" + CRLF +
	"  text-align: center;" + CRLF +
	"  padding-top: 5px;" + CRLF +
	"  padding-bottom: 5px;" + CRLF +
	"  vertical-align: top;" + CRLF +
	"  border-bottom: 1px solid #ffffff;" + CRLF +
    "}" + CRLF +
    ".li {" + CRLF +
	"  font-size: 12px;" + CRLF +
	"  text-align: center;" + CRLF +
	"  padding-top: 3px;" + CRLF +
	"  padding-bottom: 3px;" + CRLF +
	"  border-bottom: 1px dotted #ffffff;" + CRLF +
    "}";

  private String defaultPage =
    "<html>" + CRLF +
    "  <head>" + CRLF +
    "    <title>JBLS HTTP SERVER INFO</title>" + CRLF +
    "    <link href='style.css' rel='stylesheet' type='text/css' />" + CRLF +
    "  </head>" + CRLF +
    "  <body>" + CRLF +
    "    <span class=mainheader>JBLS HTTP SERVER INFORMATION</span><br />" + CRLF +
    "    <span class=mainsub>by HdX</span>" + CRLF +
    "    <hr width=325 color=#ffffff>" + CRLF +
    "    <table width=550 align=center cellpadding=3 cellspacing=0>	" + CRLF +
    "      <tr><td align=left><span class=subheader>Current Hour Statistics</span></td></tr>" + CRLF +
    "      <tr><td class=indent>JBLS Connections: <jbls></td></tr>" + CRLF +
    "      <tr><td class=indent>Admin Connections: <admin></td></tr>" + CRLF +
    "      <tr><td><span class=subheader>Server Connection Information</span></td></tr>" + CRLF +
    "    </table>" + CRLF +
    "    <table width=250 align=center cellpadding=3 cellspacing=0 border=0>" + CRLF +
    "      <tr>" + CRLF +
    "        <td align=left class=line width=150><strong>Client / Check Type</strong></td>" + CRLF +
    "        <td align=left class=line width=100><strong># of Connections</strong></td>" + CRLF +
    "        <td align=left class=line width=100><strong>Version Byte</strong></td>" + CRLF +
    "      </tr>" + CRLF +
    "      <tr><td class=li>STAR Keys</td><td class=li><sck></td><td class=li>-</td></tr>" + CRLF +
    "      <tr><td class=li>D2/WAR2 Keys</td><td class=li><d2k></td><td class=li>-</td></tr>" + CRLF +
    "      <tr><td class=li>WAR3 Keys</td><td class=li><w3k></td><td class=li>-</td></tr>" + CRLF +
    "      <tr><td class=li>STAR Checks</td><td class=li><star></td><td class=li><starvb></td></tr>" + CRLF +
    "      <tr><td class=li>W2BN Checks</td><td class=li><w2bn></td><td class=li><w2bnvb></td></tr>" + CRLF +
    "      <tr><td class=li>D2DV Checks</td><td class=li><d2dv></td><td class=li><d2dvvb></td></tr>" + CRLF +
    "      <tr><td class=li>D2XP Checks</td><td class=li><d2xp></td><td class=li><d2xpvb></td></tr>" + CRLF +
    "      <tr><td class=li>JSTR Checks</td><td class=li><jstr></td><td class=li><jstrvb></td></tr>" + CRLF +
    "      <tr><td class=li>W3XP Checks</td><td class=li><war3></td><td class=li><war3vb></td></tr>" + CRLF +
    "      <tr><td class=li>DRTL Checks</td><td class=li><drtl></td><td class=li><drtlvb></td></tr>" + CRLF +
    "    </table>" + CRLF +
    "    <table width=550 align=center cellpadding=3 cellspacing=0 border=0>" + CRLF +
    "      <tr><td align=left colspan=2><span class=subheader>Additional Information</span></td></tr>" + CRLF +
    "      <tr><td>This server requires a valid account to use: <auth></td></tr>" + CRLF +
    "      <tr><td><build></td></tr>" + CRLF +
    "    </table>" + CRLF +
    "  </body>" + CRLF +
    "</html>";

  public HTTPParse(Socket cSock){
    super("HTTPParse");
    this.socket = cSock;
	threadID = threadCount++;
    setDaemon(true);
  }

  public void run(){
    String IP = socket.getInetAddress().getHostAddress();
    Out.info("HTTP: " + threadID, "Connection thread initialized. ID " + threadID + ") Remote IP: " + IP + ".");
    try{
      out = new DataOutputStream(socket.getOutputStream());
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      Out.info("HTTP: " + threadID, "Streams created.");
      String requestLine = in.readLine();
      Out.debug("HTTP: " + threadID, "Request: " + requestLine);

      StringTokenizer tokens = new StringTokenizer(requestLine);
      String reqType = tokens.nextToken();
      if(reqType.equalsIgnoreCase("GET")){
      	String file = tokens.nextToken();
      	if(file.equalsIgnoreCase("/") ||
      	   file.equalsIgnoreCase("/index.htm") ||
      	   file.equalsIgnoreCase("/index.html")){
       	  String page = defaultPage;

       	  try{
            FileInputStream fis = new  FileInputStream("index.html");
            int size = fis.available();
            byte[] buff = new byte[size];
            fis.read(buff);
            page = new String(buff);
            fis.close();
            fis = null;
          }catch (FileNotFoundException e){}

       	  BNLSConnectionThread bCurrent = Controller.lLinkedHead;
		  int X=0;
		  while (bCurrent != null) {
		    X++;
		    bCurrent = bCurrent.getNext();
		  }
       	  page = page.replaceAll("<count>", String.valueOf(X));
       	  page = page.replaceAll("<jbls>", ""+BNLSConnectionThread.connectionCount);
       	  page = page.replaceAll("<admin>", "n/a");
       	  page = page.replaceAll("<sck>", ""+HashMain.STARKeysHashed);
       	  page = page.replaceAll("<d2k>", ""+HashMain.D2DVKeysHashed);
       	  page = page.replaceAll("<w3k>", ""+HashMain.WAR3KeysHashed);
//       	  page = page.replaceAll("<stats>", "<pre>"+Controller.aServer.settings.getStats(false)+"</pre>");
       	  page = page.replaceAll("<star>", String.valueOf(HashMain.CRevChecks[0] + HashMain.CRevChecks[1]));
       	  page = page.replaceAll("<w2bn>", String.valueOf(HashMain.CRevChecks[2]));
       	  page = page.replaceAll("<d2dv>", String.valueOf(HashMain.CRevChecks[3]));
       	  page = page.replaceAll("<d2xp>", String.valueOf(HashMain.CRevChecks[4]));
       	  page = page.replaceAll("<jstr>", String.valueOf(HashMain.CRevChecks[5]));
       	  page = page.replaceAll("<war3>", String.valueOf(HashMain.CRevChecks[6] + HashMain.CRevChecks[7]));
       	  page = page.replaceAll("<drtl>", String.valueOf(HashMain.CRevChecks[8]));
       	  page = page.replaceAll("<dshr>", String.valueOf(HashMain.CRevChecks[9]));
       	  page = page.replaceAll("<sshr>", String.valueOf(HashMain.CRevChecks[10]));
       	  page = page.replaceAll("<starvb>", hex(Constants.IX86verbytes[Constants.PRODUCT_STARCRAFT - 1]));
       	  page = page.replaceAll("<w2bnvb>", hex(Constants.IX86verbytes[Constants.PRODUCT_WAR2BNE - 1]));
       	  page = page.replaceAll("<d2dvvb>", hex(Constants.IX86verbytes[Constants.PRODUCT_DIABLO2 - 1]));
       	  page = page.replaceAll("<d2xpvb>", hex(Constants.IX86verbytes[Constants.PRODUCT_LORDOFDESTRUCTION - 1]));
       	  page = page.replaceAll("<jstrvb>", hex(Constants.IX86verbytes[Constants.PRODUCT_JAPANSTARCRAFT - 1]));
       	  page = page.replaceAll("<war3vb>", hex(Constants.IX86verbytes[Constants.PRODUCT_WARCRAFT3 - 1]));
       	  page = page.replaceAll("<drtlvb>", hex(Constants.IX86verbytes[Constants.PRODUCT_DIABLO - 1]));
       	  page = page.replaceAll("<dshrvb>", hex(Constants.IX86verbytes[Constants.PRODUCT_DIABLOSHAREWARE - 1]));
       	  page = page.replaceAll("<sshrvb>", hex(Constants.IX86verbytes[Constants.PRODUCT_STARCRAFTSHAREWARE - 1]));
       	  page = page.replaceAll("<auth>", String.valueOf(Constants.requireAuthorization));
       	  page = page.replaceAll("<build>", Constants.build);
       	  page = page.replaceAll("<news>", Constants.strNews[0]+"<br \\>"+Constants.strNews[1]+"<br \\>"+Constants.strNews[2]+"<br \\>"+Constants.strNews[3]+"<br \\>"+Constants.strNews[4]);
          writeBytes(HTTPServer.buildHeader(200, "text/html; charset=iso-8859-1", page.length()) + CRLF + page);

        }else if(file.equalsIgnoreCase("/style.css")){
          String css = null;
          try{
            FileInputStream fis = new  FileInputStream("style.css");
            int size = fis.available();
            byte[] buff = new byte[size];
            fis.read(buff);
            css = new String(buff);
            fis.close();
            fis = null;
          }catch (FileNotFoundException e){
            css = defaultCSS;
          }
          writeBytes(HTTPServer.buildHeader(200, "text/css", css.length()) + CRLF + css);

        }else if(file.equalsIgnoreCase("/stats.txt")){
          try{
            FileInputStream fis = new  FileInputStream("stats.txt");
            int size = fis.available();
            writeBytes(HTTPServer.buildHeader(200, "text/plain", size) + CRLF);
           	for (int i = 0; i < size; i++){
              int cur = fis.read();
              if(cur==-1){
                Out.error("HTTP: " + threadID, "Failed to read eintire file at " + i + "/" + size);
                return;
              }else{
                out.writeByte(cur);
            	out.flush();
              }
            }
            fis.close();
            fis = null;
          }catch (FileNotFoundException e){
            send404(file);
          }

        }else if(file.equalsIgnoreCase("/favicon.ico")){
          try{
            FileInputStream fis = new FileInputStream("favicon.ico");
            int size = fis.available();
            writeBytes(HTTPServer.buildHeader(200, "text/plain", size) + CRLF);
		    byte[] buff = new byte[4500];
		    int flag = fis.read(buff);
		    while(flag != -1) {
			  out.write(buff, 0, flag);
			  flag = fis.read(buff);
		    }
            out.flush();
            fis.close();
            fis = null;
          }catch(FileNotFoundException e){
            send404(file);
          }
        }else if(file.startsWith("/bsha/")) {
        	String input = file.substring(6);
        	
        	int[] hash = Hashing.BrokenSHA1.calcHashBuffer(input.getBytes());
        	writeBytes(HTTPServer.buildHeader(200, "text/plain", 40) + CRLF + 
        		  util.PadString.padHex(hash[0], 8) + 
  		          util.PadString.padHex(hash[1], 8) + 
  		          util.PadString.padHex(hash[2], 8) + 
  		          util.PadString.padHex(hash[3], 8) +
  		          util.PadString.padHex(hash[4], 8));

        }/*else if(file.equalsIgnoreCase("/WAR3.zip"))
          sendHashFile("WAR3", Constants.WAR3files);
        else if(file.equalsIgnoreCase("/W2BN.zip"))
          sendHashFile("W2BN", Constants.W2BNfiles);
        else if(file.equalsIgnoreCase("/STAR.zip"))
          sendHashFile("STAR", Constants.STARfiles);
        else if(file.equalsIgnoreCase("/D2DV.zip"))
          sendHashFile("D2DV", Constants.D2DVfiles);
        else if(file.equalsIgnoreCase("/D2XP.zip"))
          sendHashFile("D2XP", Constants.D2XPfiles);
        else if(file.equalsIgnoreCase("/JSTR.zip"))
          sendHashFile("JSTR", Constants.JSTRfiles);
        else if(file.equalsIgnoreCase("/DRTL.zip"))
          sendHashFile("DRTL", Constants.DRTLfiles);
        else if(file.equalsIgnoreCase("/DSHR.zip"))
          sendHashFile("DSHR", Constants.DSHRfiles);
        else if(file.equalsIgnoreCase("/SSHR.zip"))
          sendHashFile("SSHR", Constants.SSHRfiles);*/
        else{
          send404(file);
        }
      }else{
          String page =
          "<http>" + CRLF +
          "  <head>" + CRLF +
          "    <title>JBLS HTTP Server Error!</title>" + CRLF +
          "    <link href=\"style.css\" type=\"text/css\" rel=\"stylesheet\">" + CRLF +
          "  </head>" + CRLF +
          "  <body>" + CRLF +
          "    <h1>JBLS's HTTP server does not support this function.</h1>" + CRLF +
          "    <h6>Server By Hdx.</h6>" + CRLF +
          "  </body>" + CRLF +
          "</http>";
          writeBytes(HTTPServer.buildHeader(501, "text/html; charset=iso-8859-1", page.length()) + CRLF + page);

      }
    }catch (IOException e){
      e.printStackTrace();
      Out.error("HTTP: " + threadID, "IO Error:" + e.toString());
    }
    try{
      if (out    != null) out.close();
      if (in     != null) in.close();
      if (socket != null) socket.close();
    }catch(IOException e){
      Out.error("HTTP: " + threadID, "Error cloding streams: " + e.toString());
    }
    Out.debug("HTTP: " + threadID, "Closed");
    threadCount--;
  }

  private void writeBytes(String data){
    try{
      out.write(data.getBytes(), 0, data.length());
	    Out.info("HTTP: " + threadID, "Served " + data.length() + " bytes.");
    }catch(IOException e){
      Out.error("HTTP: " + threadID, "Failed to send data");
    }
  }

  private boolean createZip(String file, String[] files, int size){
  	File zips = new File("./zips");
  	if (!zips.exists()) zips.mkdirs();
	String ret = ZIP.CreateZip(files, "./zips/" + file + ".zip");
	Out.info("HTTP: " + threadID, "Create ZIP: " + ret);
    File txt = new File("./zips/" + file + ".txt");
  	try{
      if (!txt.exists()) txt.createNewFile();
      FileOutputStream fos = new FileOutputStream(txt);
      fos.write(String.valueOf(size).getBytes());
      fos.close();
    }catch(FileNotFoundException e){
      Out.info("asdf", "FNF: " + e.toString());
    }catch(IOException e){
      Out.info("asdf", "IO: " + e.toString());
    }
    if(ret.equalsIgnoreCase("success"))
      return true;
    else
      return false;
  }

  private void send404(String file){
    String page =
      "<html>" + CRLF +
      "  <head>" + CRLF +
      "    <title>File Not Found</title>" + CRLF +
      "    <link href=\"style.css\" type=\"text/css\" rel=\"stylesheet\">" + CRLF +
      "  </head>" + CRLF +
      "  <body>" + CRLF +
      "    <h1>Sorry but the file <i>" + file +"</i> could not be found on this server.</h1>" + CRLF +
      "  </body>" + CRLF +
      "</html>";

    writeBytes(HTTPServer.buildHeader(404, "text/html; charset=iso-8859-1", page.length()) + CRLF + page);
  }

  private String hex(int value){
    String s = Integer.toHexString(value);
    if (s.length() == 1)
      s = "0" + s;
    return "0x" + s.toUpperCase();
  }
}
