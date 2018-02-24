/**
 * Project Name:  JBLS ( Java Bot Login Server), or however you want to make the acronym
 *
 * >>>>>  http://www.JBLS.org/
 *
 * @author The-FooL (fool@the-balance.com)
 * @author Hdx      (Hdx@JBLS.org)
 *   Other Contributors:
 *     Joe
 *     Ron (iago@JavaOp.com)
 *
 *
 * Program Emulates a BNLS Server, allowing users to run their own server
 * and connect bots that use the BNLS protocol to it.
 *
 *   I am not the best at Programming, but (most of)what is in here works.
 *   Please feel free to modify/change any parts you like, and submit them back to me.
 *   This project is now open source, hopefully few scams will arise from this.
 *
 *   I also take no responsibility for your use of this program.
 *
 * @version
 *  V3 - Lockdown compatible
 *       Changed Checkrevision sceam to allow easier future updates
 *       Changed Admin interface to BotNet
 *
 *  V2 - Constants are no longer labeled as final so that the
 *       Constants.class file can be replaced when updates are needed
 *
 *  VCurrent - CVS Version to be worked on
 *     *The-FooL:
 *     -Fixed the issue that required double connections in order to get
 *        JBLS to work.
 *     -Converted All System.out statements to the static Out Class
 *     -Set up Admin Server Thread, and connection sub-threads
 *     -Created settings class, and IP HashTable/Auth classes
 */
import util.Controller;
import util.Constants;
import util.Out;
import util.cSettings;
import util.BotNet;
import util.Statistics;
import BNLSProtocol.BNLSServer;
import HTTP.HTTPServer;

public class Main {

  /** Main Method - Starting point for program */
  public static void main(String[] args) throws Exception{
    cSettings.LoadSettings();
    cSettings.SaveSettings();
    
    Out.setDefaultOutputStream();
    Out.println("Main", "Java Battle.Net Login Server - (JBLS)  http://www.JBLS.org/");
    Out.println("Main", "Build: " + Constants.build);

    Out.println("Main", "JBLS Started");

    //Start BNLS Server
    Out.println("Main","Loading JBLS Server");
    Controller.jServer=new BNLSServer();
    Controller.jServer.start();

    //Start HTTP Server
    if(Constants.RunHTTP){
      Out.println("Main","Loading HTTP Server");
      Controller.hServer=new HTTPServer();
      Controller.hServer.start();
    }
    
    if(Constants.RunAdmin && Constants.BotNetServer.length() > 0 && 
    		Constants.BotNetBotID.length() > 0 && Constants.BotNetHubPW.length() > 0 &&
    		Constants.BotNetUsername.length() > 0 && Constants.BotNetPassword.length() > 0){
      BotNet bn = new BotNet();
      bn.start();
    }   
	
	if(Constants.LogStats){
	  Controller.stats = new Statistics();
	  Controller.stats.connect();
	}
  }
}//end main class