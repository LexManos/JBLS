package util;

import java.net.*;
import java.io.*;
import java.util.*;



/*****************************
 *BotID: RivalBot            *
 *HubPW: b8f9b319f223ddcc38  *
 *Databse: JBLS              *
 *Password: 0a9115d19f4cf125 *
 *Database: MiniCouncil      *
 *Password:fbf372f087c6dc1626d
 *This will allow an admin   *
 *interface to JBLS through  *
 *botnet.                    *
 *****************************/
public class BotNet extends Thread{
  private static final byte PACKET_IDLE             = 0x00;
  private static final byte PACKET_LOGON            = 0x01; 
  private static final byte PACKET_STATSUPDATE      = 0x02; 
  //private static final byte PACKET_DATABASE         = 0x03; 
  //private static final byte PACKET_MESSAGE          = 0x04; 
  //private static final byte PACKET_CYCLE            = 0x05; 
  private static final byte PACKET_USERINFO         = 0x06; 
  private static final byte PACKET_USERLOGGINGOFF   = 0x07; 
  //private static final byte PACKET_BROADCASTMESSAGE = 0x07; 
  private static final byte PACKET_COMMAND          = 0x08; 
  private static final byte PACKET_CHANGEDBPASSWORD = 0x09; 
  private static final byte PACKET_BOTNETVERSION    = 0x0A; 
  private static final byte PACKET_BOTNETCHAT       = 0x0B; 
  private static final byte PACKET_ACCOUNT          = 0x0D; 
  //private static final byte PACKET_CHATDROPOPTIONS  = 0x10;  

  private Hashtable<Integer, String> names = new Hashtable<Integer, String>();
  private Hashtable<String, Integer> ids = new Hashtable<String, Integer>();
  
  private InputStream in = null;
  private OutputStream out = null;
  private Socket bnsck = null;
  
  public static void main(String[] args) { }
  
  public void run() {
    try {
      bnsck = new Socket();
      SocketAddress sa = new InetSocketAddress(Constants.BotNetServer, 0x5555);
      
      // Try to connect every 10 seconds until successful.
      while (!bnsck.isConnected()) {
    	  try {
    		  bnsck.connect(sa);
    		  Out.info("BotNet", "Connected to BotNet!");
    	  } catch (IOException ioEx) {
    		  // Wait a few seconds and try again
    		  try {
    			  Thread.sleep(1000 * 10);
    		  } catch (InterruptedException iEx) {
    			  Out.info("BotNet", "BotNet thread interrupted.");
    			  return;
    		  }
    		  bnsck = new Socket();	// reset the socket
    	  }
      }
      
      in = bnsck.getInputStream();
      out = bnsck.getOutputStream();
      
      OutPacketBuffer output = new OutPacketBuffer(PACKET_LOGON);
      output.addNTString(Constants.BotNetBotID);
      output.addNTString(Constants.BotNetHubPW);
      send(output, out);
      
      while(true){
        in.read(); //protocol
        int id = in.read();
        short plength = (short)(((in.read() << 0) & 0x000000FF) | ((in.read() << 8) & 0x0000FF00));
        if(id == -1) {
          Out.info("BotNet", "Connection terminated.");
          in.close();
          out.close();
          bnsck.close();
          
          // Start over.
          run();
        }
        
        int i = 0;
        int read = 4;
        Buffer pData = new Buffer();
        while(read < plength){
          i = in.read();
          if(i == -1) {
            Out.info("BotNet", "Connection terminated.");
            in.close();
            out.close();
            bnsck.close();
            
            // Start over.
            run();
          }
          
          read++;
          pData.addByte((byte)i);
        }
        parse(pData, out, id);
      }
    } catch (SocketException sockEx) {
    	Out.error("BotNet", sockEx.getMessage());
    	run();
    } catch (Exception e) {
      Out.error("BotNet", "BotNet error: " + e.toString());
    }  
    
    try {
    	in.close();
    	out.close();
    	bnsck.close();
    } catch (IOException ioEx) {
    	// Ignore since we're stopping.
    }
  }
  private void send(OutPacketBuffer data, OutputStream out){
    try{
      out.write(data.getBuffer());
      out.flush();
    }catch(Exception e){}
  }
  private void parse(Buffer data, OutputStream out, int ID){
    switch (ID){
      case PACKET_IDLE:
        OutPacketBuffer buff = new OutPacketBuffer(PACKET_IDLE);
        send(buff, out);
        break;
      
      case PACKET_LOGON:
        if(Constants.debugInfo){
          data.removeDWord();
          Out.debug("BotNet", "External IP address: " + 
          (((int)data.removeChar())&0xFF) + "." + (((int)data.removeChar())&0xFF) + "." + 
          (((int)data.removeChar())&0xFF) + "." + (((int)data.removeChar())&0xFF));
        }
        buff = new OutPacketBuffer(PACKET_BOTNETVERSION);
        buff.addDWord(1);
        buff.addDWord(1);
        send(buff, out);
        
        buff = new OutPacketBuffer(PACKET_ACCOUNT);
        buff.addDWord(0);
        buff.addNTString(Constants.BotNetUsername);
        buff.addNTString(Constants.BotNetPassword);
        send(buff, out);
        
        buff = new OutPacketBuffer(PACKET_STATSUPDATE);
        buff.addNTString("JBLSServer");
        buff.addNTString("<Not logged on>");
        buff.addDWord(-1);
        buff.addNTString(Constants.BotNetDatabase);
        buff.addDWord(0);
        send(buff, out);
        break;
        
      case PACKET_USERINFO:
        if(data.size() < 8) return;
        int id = data.removeDWord();
        data.removeDWord();//Access
        data.removeDWord();//Admin
        data.removeNTString();//Bnet Name
        data.removeNTString();//Bnet Channel
        data.removeDWord();//Bnet server
        String accountName = data.removeNTString();
        data.removeNTString();//Database
        if(accountName.equals("") == false){
          names.put(id, accountName);
          ids.put(accountName.toLowerCase(), id);
        }
        break;
        
      case PACKET_USERLOGGINGOFF:
        id = data.removeDWord();
        String name = names.get(id);
        names.remove(id);
        if (name != null) ids.remove(name.toLowerCase());
        break;
        
      case PACKET_STATSUPDATE:
        data.removeDWord();
        if(data.size() > 0){
          int flags = data.removeDWord();
          Out.debug("BotNet", "Stats update: " + flags);
        }
        break;
        
      case PACKET_COMMAND:
        int error = data.removeDWord();
        id = (int)data.removeByte();
        data.removeWord(); //good len
        data.removeWord(); //extra
        if(error == 1) Out.debug("BotNet", "Command error: Unknown packet ID: " + id);
        Out.debug("BotNet", "Command error: " + id + ": " + error);
        break;
        
      case PACKET_CHANGEDBPASSWORD:
      case PACKET_BOTNETVERSION:
        if(Constants.debugInfo){
          switch(data.removeDWord()){
            case 0x04: Out.debug("BotNet", "Server Version: 4");
          }
        }
        break;
        
      case PACKET_BOTNETCHAT:
        int command = data.removeDWord();
        data.removeDWord(); //action
        id = data.removeDWord();
        String message = data.removeNTString();
        if(command == 2) parseCommand(id, message, out);
        break;
        
      case PACKET_ACCOUNT:
        command = data.removeDWord();
        boolean success = (data.removeDWord() == 0 ? false : true);
        switch(command){
          case 0x00:
            if(success) {
              Out.debug("BotNet", "Logged in to account " + Constants.BotNetUsername);
              buff = new OutPacketBuffer(PACKET_USERINFO);
              send(buff, out);
            }else{
              Out.debug("BotNet", "Failed to login, attempting to create account " + Constants.BotNetUsername);
              buff = new OutPacketBuffer(PACKET_ACCOUNT);
              buff.addDWord(2);
              buff.addNTString(Constants.BotNetUsername);
              buff.addNTString(Constants.BotNetPassword);
              send(buff, out);
            }
            break;
          case 0x02:
            if(success){
              Out.debug("BotNet", "Account created, logging in.");
              buff = new OutPacketBuffer(PACKET_ACCOUNT);
              buff.addDWord(0);
              buff.addNTString(Constants.BotNetUsername);
              buff.addNTString(Constants.BotNetPassword);
              send(buff, out);
            }else{
              Out.debug("BotNet", "Failed to create account. Terminating connection.");
              return;
            }
            break;
         }
         break;
         
      default: Out.info("BotNet", "Unknown Packet ID: " + Integer.toHexString(ID));
    }   
  }

  private void parseCommand(int ID, String message, OutputStream out){
    if(names.get(ID) == null) return;
    int access = Integer.parseInt(Ini.ReadIni("./admins.ini", "Admins", names.get(ID), "0"), 10);
    if (access == 0) return;
    String[] args = message.split(" ");
    StringBuffer response = new StringBuffer();
    Out.info("Admin", names.get(ID) + " >> " + message);
    if(args[0].equalsIgnoreCase("respond")){
       response.append("Fuck you man! I'm here, I responded, now leave me alone!\r");
    }else if(args[0].equalsIgnoreCase("reload")){
      cSettings.LoadSettings();
      response.append("Settings file reloaded\r");
    }else if(args[0].equalsIgnoreCase("ips")){
      response.append(BNLSProtocol.IpAuth.getList());
    }else if(args[0].equalsIgnoreCase("SetIp")){
      int newstat = Integer.parseInt(args[2]);
      String old = BNLSProtocol.IpAuth.getIpStatus(args[1]);
      String news = BNLSProtocol.IpAuth.getStatus(newstat);
      BNLSProtocol.IpAuth.setIpStatus(args[1], newstat);
      response.append("Set IP ").append(args[1]).append(" status from ");
      response.append(old).append(" to ").append(news).append(".\r");
    }else if(args[0].equalsIgnoreCase("IPBan")){
      BNLSProtocol.IpAuth.setIpStatus(args[1], BNLSProtocol.IpAuth.IPBANNING);
      response.append("IP ").append(args[1]).append(" is now Banned.\r");
    }else if(args[0].equalsIgnoreCase("IPAuth")){
      response.append(BNLSProtocol.IpAuth.getIpString(args[1])).append("\r");
    }else if(args[0].equalsIgnoreCase("set")){
      Ini.WriteIni("./settings.ini", args[1], args[2], message.substring(args[1].length() + args[2].length() + 6));
    }else if(args[0].equalsIgnoreCase("get")){
      String data = Ini.ReadIni("./settings.ini", args[1], args[2], "");
      response.append("Value: ").append(data).append("\r");
    }else if(args[0].equalsIgnoreCase("bnftp")){
      for(int x = 2; x < args.length; x++){
        BNFTP bnftp = new BNFTP(args[1], 6112, args[x], this, ID);
        bnftp.start();
      }
    }else if(args[0].equalsIgnoreCase("kill")){
      if(args[1].equalsIgnoreCase("all")){
        if(Controller.hServer != null) Controller.hServer.destroyAllConnections();
        if(Controller.jServer != null) Controller.jServer.destroyAllConnections();
        response.append("All HTTP and JBLS connection threads terminated.\r");
      }else if(args[1].equalsIgnoreCase("JBLS")){
        if(Controller.jServer != null) Controller.jServer.destroyAllConnections();
        response.append("All JBLS connection threads terminated.\r");
      }else if(args[1].equalsIgnoreCase("HTTP")){
        if(Controller.hServer != null) Controller.hServer.destroyAllConnections();
        response.append("All HTTP connection threads terminated.\r");
      }else
        response.append("Unknown command, Propper usage: kill [All|HTP|JBLS]\r");
    }else if(args[0].equalsIgnoreCase("stop")){
      if(args[1].equalsIgnoreCase("all")){
        //if(Controler.hServer != null) Controler.hServer.destroyAllConnections();
        Controller.stopJBLS();
        response.append("HTTP and JBLS Servers closed.\r");
      }else if(args[1].equalsIgnoreCase("JBLS")){
        Controller.stopJBLS();
        response.append("JBLS Server closed.\r");
      }else if(args[1].equalsIgnoreCase("HTTP")){
        //if(Controler.hServer != null) Controler.hServer.destroyAllConnections();
        response.append("HTTP Server closed.\r");
      }else
        response.append("Unknown command, Propper usage: stop [All|HTP|JBLS]\r");
    }else if(args[0].equalsIgnoreCase("start")){
      if(args[1].equalsIgnoreCase("all")){
        //if(Controler.hServer != null) Controler.hServer.destroyAllConnections();
        Controller.startJBLS();
        response.append("HTTP and JBLS Servers created.\r");
      }else if(args[1].equalsIgnoreCase("JBLS")){
        Controller.startJBLS();
        response.append("JBLS Server created.\r");
      }else if(args[1].equalsIgnoreCase("HTTP")){
        //if(Controler.hServer != null) Controler.hServer.destroyAllConnections();
        response.append("HTTP Server ccreated.\r");
      }else
        response.append("Unknown command, Propper usage: start [All|HTP|JBLS]\r");
    }else if(args[0].equalsIgnoreCase("restart")){
      if(args[1].equalsIgnoreCase("all")){
        //if(Controler.hServer != null) Controler.hServer.destroyAllConnections();
        Controller.restartJBLS();
        response.append("HTTP and JBLS Servers restarted.\r");
      }else if(args[1].equalsIgnoreCase("JBLS")){
        Controller.restartJBLS();
        response.append("JBLS Server restarted.\r");
      }else if(args[1].equalsIgnoreCase("HTTP")){
        //if(Controler.hServer != null) Controler.hServer.destroyAllConnections();
        response.append("HTTP Server restarted.\r");
      }else
        response.append("Unknown command, Propper usage: restart [All|HTP|JBLS]\r");
    }else if(args[0].equalsIgnoreCase("shutdown")){
      whisper(ID, "Server shutting down, Goodbye crul world!!! I will miss thee!");
      Controller.shutdown();
    }else if(args[0].equalsIgnoreCase("clearcache")){
      Hashing.CheckRevisionV1.clearCache();
      Hashing.CheckRevisionV2.clearCache();
      Hashing.CheckRevisionV3.clearCache();
      response.append("Cache cleared\r");
    }else if(args[0].equalsIgnoreCase("help")){
      response.append("JBLS Admin Commands:\r");
      response.append("  BNFTP [Server] [Files ...] - The JBLS server will attempt to download these files useing Battle.net's FTP protocol.\r");
      response.append("  ClearCache - Clears all CheckRevision based caches.\r");
      response.append("  Get [Header] [Property] - Read a INI value from settings.ini.\r");
      response.append("  Help - This message.\r");
      response.append("  IPAuth [IP] - Displays the statistice for the specified IP\r");
      response.append("  IPBan [IP] - Adds the specified IP to the IPBan list.\r");
      response.append("  IPs - List all IP address that have special atributes on the server.\r");
      response.append("  Kill [All|HTTP|JBLS] - Destroys all client threads associated with the specified server.\r");
      response.append("  Respond - Test command, Don't use.\r");
      response.append("  Restart [All|HTTP|JBLS] - Restarts the server, Kills all connections and opens up for new ones.\r");
      response.append("  Set [Header] [Property] [Value] - Raw Settings.ini access, Writes the data to Settings.ini.\r");
      response.append("  SetIp [IP] [Status] - Manually sets the status of the IP to the number specified.\r");
      response.append("  Shutdown - Destroys all connection threads, and exits the daemon.\r");
      response.append("  Start [All|HTTP|JBLS] - Enable's the noted server.\r");
      response.append("  Stop [All|HTTP|JBLS] - Disable's the noted server.\r");
    }else{
      response.append("Unknown command.");
    }
      //Stats
      //Reload
      //Patch [zip] [verbyte] [client]
      //download [url]
    if(response.length() > 0){
      String[] reply = response.toString().split("\r");
      for(String rep:reply)
        whisper(ID, rep);
    }
  }

  private class OutPacketBuffer extends Buffer{
    public static final long serialVersionUID=0x1234;
    private byte packetID;
    public OutPacketBuffer(){ super(); }
    public OutPacketBuffer(byte ID){ packetID=ID; }
    public OutPacketBuffer(int ID){packetID=(byte)ID;}
    public byte[] getBuffer(){
         byte[] ret = new byte[size()+4];

        System.arraycopy(super.getBuffer(), 0, ret, 4, size());//Copy Buffer Into a New Array
        
        ret[0] = (byte) 0x01;
        ret[1] = (byte) packetID;
        ret[2] = (byte) ((ret.length >> 0) & 0x000000FF);
        ret[3] = (byte) ((ret.length >> 8) & 0x000000FF);
        return ret;
    }
  }
  public void whisper(int id, String message){
    OutPacketBuffer buff = new OutPacketBuffer(PACKET_BOTNETCHAT);
    buff.addDWord(2);
    buff.addDWord(0);
    buff.addDWord(id);
    buff.addNTString(message);
    send(buff, out);
  }
}