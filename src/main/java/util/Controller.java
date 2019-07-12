/*
 * Created on Mar 4, 2005
 */
package util;
import BNLSProtocol.BNLSServer;
import BNLSProtocol.BNLSConnectionThread;
import HTTP.HTTPServer;

/**
 * This Class contains static references to all the main and important
 * classes.  It has full control of all the threads, and provides a way
 * for the admin, etc. classes to interact.
 */
public class Controller {
  public static HTTPServer hServer;
	public static BNLSServer jServer;
	public static BNLSConnectionThread lLinkedHead = null;
	public static ThreadGroup jConnectionThreads=new ThreadGroup("JBLS");
	public static Statistics stats = null;

	/**
	 * Shuts down the program, terminating all threads
	 */
	public static void shutdown(){
		Out.println("Control","Shutting Down...");
		if(jServer!=null){
            jServer.destroyAllConnections();
			jServer.closeSocket();
	    }
		jServer=null;
		ThreadGroup tg=Thread.currentThread().getThreadGroup();
		tg.list();
		
		Out.println("Control","Terminated");
		System.exit(0);
	}

	/**
	 * Restarts the JBLS Server Thread Only
	 *
	 */
	public static void restartJBLS(){
    stopJBLS();
    startJBLS();
	}

	public static void stopJBLS(){
     Out.println("Control","Stoping JBLS Server Thread");

		if(jServer!=null){
      jServer.destroyAllConnections();
			jServer.closeSocket();
		}
		jServer=null;

		try{
			Thread.sleep(1000);
		}catch(InterruptedException e){}
	}

	public static void startJBLS(){

		Out.println("Control","Starting JBLS Server Thread");
		if(jServer!=null) jServer.closeSocket();
		jServer=null;

		try{
			Thread.sleep(1000);
		}catch(InterruptedException e){}

		jServer=new BNLSServer();
		jServer.start();
	}

	/**
	 * Restarts all the server threads, reloads the settings
	 */
	public static void restartAll(){

		Out.println("Control","Restarting all Threads");
		if(jServer!=null){
      jServer.destroyAllConnections();
			jServer.closeSocket();
		}
		jServer=null;

		jServer.destroyAllConnections();

		try{
			Thread.sleep(1000);
		}catch(InterruptedException e){}

		jServer=new BNLSServer();
		jServer.start();
	}
	
	/**
	 * Destroys the JBLS Server
	 *
	 */
	public static void disableJBLSServer(){
		if(jServer!=null)
			jServer.closeSocket();
		jServer=null;
		Out.debug("Control","JBLS Server Disabled");
	}
}