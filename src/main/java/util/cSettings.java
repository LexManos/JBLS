package util;

import util.Constants;

public class cSettings {
   private static String file = "./settings.ini";
   public static void LoadSettings() {
   	//Load the Misc
   	Constants.ArchivePath = Ini.ReadIni(file, "Main", "Archives", Constants.ArchivePath);
    Constants.DownloadPath = Ini.ReadIni(file, "Main", "DownloadPath", Constants.DownloadPath);
    Constants.LogFilePath = Ini.ReadIni(file, "Main", "Logs", Constants.LogFilePath);
    
   	Constants.BNLSPort = Integer.parseInt(Ini.ReadIni(file, "Main", "BNLSPort", Integer.toString(Constants.BNLSPort)), 10);
   	Constants.maxThreads = Integer.parseInt(Ini.ReadIni(file, "Main", "MaxThreads", Integer.toString(Constants.maxThreads)), 10);
   	   	
    Constants.RunHTTP = Boolean.valueOf(Ini.ReadIni(file, "Main", "EnableHTTP", Boolean.toString(Constants.RunHTTP)));
   	Constants.HTTPPort = Integer.parseInt(Ini.ReadIni(file, "Main", "HTTPPort", Integer.toString(Constants.HTTPPort)), 10);
   	
   	Constants.ipAuthStatus = Integer.parseInt(Ini.ReadIni(file, "Main", "IPAuth", Integer.toString(Constants.ipAuthStatus)), 10);
   	Constants.requireAuthorization = Boolean.valueOf(Ini.ReadIni(file, "Main", "RequireAuth", Boolean.toString(Constants.requireAuthorization)));
   	
   	Constants.displayPacketInfo = Boolean.valueOf(Ini.ReadIni(file, "Main", "DisplayPacketInfo", Boolean.toString(Constants.displayPacketInfo)));
   	Constants.displayParseInfo = Boolean.valueOf(Ini.ReadIni(file, "Main", "DisplayParseInfo", Boolean.toString(Constants.displayParseInfo)));
   	Constants.debugInfo = Boolean.valueOf(Ini.ReadIni(file, "Main", "DisplayDebugInfo", Boolean.toString(Constants.debugInfo)));
   	Constants.enableLogging = Boolean.valueOf(Ini.ReadIni(file, "Main", "EnableLogging", Boolean.toString(Constants.enableLogging)));
   	Constants.logKeepDuration = Integer.parseInt(Ini.ReadIni(file, "Main", "LogFileKeepDays", Integer.toString(Constants.logKeepDuration)));
    
   	Constants.RunAdmin = Boolean.valueOf(Ini.ReadIni(file, "Admin", "EnableAdmin", Boolean.toString(Constants.RunAdmin)));
   	Constants.BotNetBotID = Ini.ReadIni(file,  "Admin",  "BotNetBotID", Constants.BotNetBotID);
   	Constants.BotNetHubPW = Ini.ReadIni(file,  "Admin",  "BotNetHubPW", Constants.BotNetHubPW);
   	Constants.BotNetDatabase = Ini.ReadIni(file, "Admin", "BotNetDatabase", Constants.BotNetDatabase);
   	Constants.BotNetUsername = Ini.ReadIni(file, "Admin", "BotNetUsername", "");
   	Constants.BotNetPassword = Ini.ReadIni(file, "Admin", "BotNetPassword", "");
    Constants.BotNetServer   = Ini.ReadIni(file, "Admin", "BotNetServer", Constants.BotNetServer);
   	

   	Constants.LogStats = Boolean.valueOf(Ini.ReadIni(file, "Statistics", "Enable", Boolean.toString(Constants.LogStats)));
   	Constants.StatsLogIps = Boolean.valueOf(Ini.ReadIni(file, "Statistics", "Include IPs", Boolean.toString(Constants.StatsLogIps)));
   	Constants.StatsLogConns = Boolean.valueOf(Ini.ReadIni(file, "Statistics", "Connections", Boolean.toString(Constants.StatsLogConns)));
   	Constants.StatsLogCRevs = Boolean.valueOf(Ini.ReadIni(file, "Statistics", "CheckRevisions", Boolean.toString(Constants.StatsLogCRevs)));
   	Constants.StatsLogBotIDs = Boolean.valueOf(Ini.ReadIni(file, "Statistics", "BotIDs", Boolean.toString(Constants.StatsLogBotIDs)));
   	Constants.StatsCheckSchema = Boolean.valueOf(Ini.ReadIni(file, "Statistics", "Check Schema", Boolean.toString(Constants.StatsCheckSchema)));
   	Constants.StatsQueue = Integer.parseInt(Ini.ReadIni(file, "Statistics", "Queue", Integer.toString(Constants.StatsQueue)), 10);
	Constants.StatsUsername = Ini.ReadIni(file, "Statistics", "Username", "");
   	Constants.StatsPassword = Ini.ReadIni(file, "Statistics", "Password", "");
   	Constants.StatsDatabase = Ini.ReadIni(file, "Statistics", "Database", Constants.StatsDatabase);
    Constants.StatsServer   = Ini.ReadIni(file, "Statistics", "Server", Constants.StatsServer);
   	

     //Load IX86 Versioning Settings
     for(int x = 0; x < Constants.prods.length; x++){
    	String header = Constants.prods[x] + "-IX86";
     	Constants.IX86files[x][0] = Ini.ReadIni(file, header, "HashPath", Constants.IX86files[x][0]);
     	Constants.IX86files[x][1] = Ini.ReadIni(file, header, "Exe", Constants.IX86files[x][1]);
     	Constants.IX86files[x][2] = Ini.ReadIni(file, header, "Storm", Constants.IX86files[x][2]);
     	Constants.IX86files[x][3] = Ini.ReadIni(file, header, "Network", Constants.IX86files[x][3]);
     	Constants.IX86files[x][4] = Ini.ReadIni(file, header, "Screen", Constants.IX86files[x][4]);
     	Constants.IX86verbytes[x] = Integer.parseInt(Ini.ReadIni(file, header, "VerByte", Integer.toHexString(Constants.IX86verbytes[x])), 16);
     	Constants.IX86versions[x] = Ini.ReadIni(file, header, "Version", Constants.IX86versions[x]);
     	Constants.IX86certs[x] = Ini.ReadIni(file, header, "Cert", Constants.IX86certs[x]);
     }
   }
   public static void SaveSettings(){
   	
   	//Save the Misc
   	Ini.WriteIni(file, "Main", "Archives", Constants.ArchivePath);
   	Ini.WriteIni(file, "Main", "DownloadPath", Constants.DownloadPath);
   	Ini.WriteIni(file, "Main", "BNLSPort", Integer.toString(Constants.BNLSPort));
   	Ini.WriteIni(file, "Main", "MaxThreads", Integer.toString(Constants.maxThreads));
   	   	
   	Ini.WriteIni(file, "Main", "EnableHTTP", Boolean.toString(Constants.RunHTTP));
   	Ini.WriteIni(file, "Main", "HTTPPort", Integer.toString(Constants.HTTPPort));
   	
   	Ini.WriteIni(file, "Main", "IPAuth", Integer.toString(Constants.ipAuthStatus));
   	Ini.WriteIni(file, "Main", "RequireAuth", Boolean.toString(Constants.requireAuthorization));
   	
   	Ini.WriteIni(file, "Main", "DisplayPacketInfo", Boolean.toString(Constants.displayPacketInfo));
   	Ini.WriteIni(file, "Main", "DisplayParseInfo", Boolean.toString(Constants.displayParseInfo));
   	Ini.WriteIni(file, "Main", "DisplayDebugInfo", Boolean.toString(Constants.debugInfo));
    
   	Ini.WriteIni(file, "Admin", "EnableAdmin", Boolean.toString(Constants.RunAdmin));
   	Ini.WriteIni(file, "Admin", "BotNetUsername", Constants.BotNetUsername);
   	Ini.WriteIni(file, "Admin", "BotNetPassword", Constants.BotNetPassword);
   	Ini.WriteIni(file, "Admin", "BotNetServer", Constants.BotNetServer);
   	
   	Ini.WriteIni(file, "Statistics", "Enable", Boolean.toString(Constants.LogStats));
   	Ini.WriteIni(file, "Statistics", "Include IPs", Boolean.toString(Constants.StatsLogIps));
   	Ini.WriteIni(file, "Statistics", "Connections", Boolean.toString(Constants.StatsLogConns));
   	Ini.WriteIni(file, "Statistics", "CheckRevisions", Boolean.toString(Constants.StatsLogCRevs));
   	Ini.WriteIni(file, "Statistics", "Check Schema", Boolean.toString(Constants.StatsCheckSchema));
   	Ini.WriteIni(file, "Statistics", "BotIDs", Boolean.toString(Constants.StatsLogBotIDs));
   	Ini.WriteIni(file, "Statistics", "Queue", Integer.toString(Constants.StatsQueue));
   	Ini.WriteIni(file, "Statistics", "Username", Constants.StatsUsername);
   	Ini.WriteIni(file, "Statistics", "Password", Constants.StatsPassword);
   	Ini.WriteIni(file, "Statistics", "Database", Constants.StatsDatabase);
    Ini.WriteIni(file, "Statistics", "Server", Constants.StatsServer);

     //Save IX86 Versioning Settings
     for(int x = 0; x < Constants.prods.length; x++){
    	String header = Constants.prods[x] + "-IX86";
     	Ini.WriteIni(file, header, "HashPath", Constants.IX86files[x][0]);
     	Ini.WriteIni(file, header, "Exe", Constants.IX86files[x][1]);
     	Ini.WriteIni(file, header, "Storm", Constants.IX86files[x][2]);
     	Ini.WriteIni(file, header, "Network", Constants.IX86files[x][3]);
     	Ini.WriteIni(file, header, "Screen", Constants.IX86files[x][4]);
     	Ini.WriteIni(file, header, "VerByte", 
     	  PadString.padString(Integer.toHexString(Constants.IX86verbytes[x]), 2, '0'));
     	Ini.WriteIni(file, header, "Version", Constants.IX86versions[x]);
     	Ini.WriteIni(file,  header, "Cert", Constants.IX86certs[x]);
     }
     
   }
  }