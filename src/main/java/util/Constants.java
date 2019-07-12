package util;

public final class Constants{
    
    public static final byte PLATFORM_INTEL   = 0x01;
    public static final byte PLATFORM_POWERPC = 0x02;
    public static final byte PLATFORM_MACOSX  = 0x03;

    public static final byte PRODUCT_STARCRAFT         = 0x01;
    public static final byte PRODUCT_BROODWAR          = 0x02;
    public static final byte PRODUCT_WAR2BNE           = 0x03;
    public static final byte PRODUCT_DIABLO2           = 0x04;
    public static final byte PRODUCT_LORDOFDESTRUCTION = 0x05;
    public static final byte PRODUCT_JAPANSTARCRAFT    = 0x06;
    public static final byte PRODUCT_WARCRAFT3         = 0x07;
    public static final byte PRODUCT_THEFROZENTHRONE   = 0x08;
    public static final byte PRODUCT_DIABLO            = 0x09;
    public static final byte PRODUCT_DIABLOSHAREWARE   = 0x0A;
    public static final byte PRODUCT_STARCRAFTSHAREWARE= 0x0B;
    public static String[] prods = {"STAR", "SEXP", "W2BN", "D2DV", "D2XP", "JSTR", "WAR3", "W3XP", "DRTL", "DSHR", "SSHR"};
    public static String[][] IX86files = {
        {"IX86/STAR/", "StarCraft.exe",       "Storm.dll",    "NULL",   	  "STAR.bin"},
        {"IX86/STAR/", "StarCraft.exe",       "Storm.dll",    "NULL",   	  "STAR.bin"},
        {"IX86/W2BN/", "Warcraft II BNE.exe", "Storm.dll",    "Battle.snp",   "W2BN.bin"},
        {"IX86/D2DV/", "Game.exe",            "NULL", 		  "NULL", 		  "D2DV.bin"},
        {"IX86/D2XP/", "Game.exe",            "NULL", 		  "NULL", 		  "D2XP.bin"},
        {"IX86/JSTR/", "StarcraftJ.exe",      "Storm.dll",    "Battle.snp",   "JSTR.bin"},
        {"IX86/WAR3/", "Warcraft III.exe",    "NULL",    	  "NULL",     	  "WAR3.bin"},
        {"IX86/WAR3/", "Warcraft III.exe",    "NULL",    	  "NULL",     	  "WAR3.bin"},
        {"IX86/DRTL/", "Diablo.exe",          "Storm.dll",    "Battle.snp",   "DRTL.bin"},
        {"IX86/DSHR/", "Diablo_s.exe",        "Storm.dll",    "Battle.snp",   "DSHR.bin"},
        {"IX86/SSHR/", "Starcraft.exe",       "Storm.dll",    "Battle.snp",   "SSHR.bin"}
    };
    public static int[] IX86verbytes = {0xD3, 0xD3, 0x4f, 0x0e, 0x0e, 0xa9, 0x1E, 0x1E, 0x2a, 0x2a, 0xa5};
    public static String[] IX86versions = {"", "", "2.0.2.1", "1.14.3.71", "1.14.3.71", "", "", "", "2001, 5, 18, 1", "", ""};
    public static String[] IX86certs = {"", "", "", "", "", "", "", "", "", "", ""};
    
    public static String ArchivePath = "DLLs/";
    public static String LogFilePath = "./Logs/";
    
    public static String build="Build V3.1 Bug Fixes, SQL Stats tracking. (10-14-07)";
    //public static String build="Build V3.0 BotNet Admin, Lockdown, Legacy Clients.(07-07-07)";
    //public static String build="Build V2.9 Remote admin, extended admin commands w/ JSTR support.(01/18/06)";
    public static int maxThreads=500;
    public static int BNLSPort=9367;
    public static boolean requireAuthorization=false;
    public static boolean trackStatistics=true;
    public static int ipAuthStatus=1; //default is IPBans on

    public static boolean displayPacketInfo=true;
    public static boolean displayParseInfo=false;
    public static boolean debugInfo=false;
    public static boolean enableLogging = false;
    public static int logKeepDuration = 7;

    public static boolean RunAdmin = false;
    public static String   BotNetBotID    = "";
    public static String   BotNetHubPW    = "";
    public static String   BotNetDatabase = "";
    public static String   BotNetUsername = "";
    public static String   BotNetPassword = "";
    public static String   BotNetServer   = "www.valhallalegends.com";
	
    public static boolean LogStats         = false;
    public static String  StatsUsername    = "";
    public static String  StatsPassword    = "";
    public static String  StatsDatabase    = "";
    public static String  StatsServer      = "localhost";
    public static int     StatsQueue       = 10;
    public static boolean StatsLogIps      = false;
    public static boolean StatsLogCRevs    = true;
    public static boolean StatsLogBotIDs   = true;
    public static boolean StatsLogConns    = true;
    public static boolean StatsCheckSchema = true;
    
    public static String DownloadPath = "./";
    
    public static boolean RunHTTP = true;
    public static int     HTTPPort = 81;
    
    public static int lngServerVer=0x01;
    public static int numOfNews=0;
    public static String[] strNews={"", "", "", "", ""};
}
