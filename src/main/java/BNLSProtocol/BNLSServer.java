/*
 * Created on Sep 19, 2004
 *
 * This is the actual Server for the BNLS
 * Listens for connections and parses the incomming data
 */

package BNLSProtocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.BindException;

import util.Controller;
import util.Out;
import util.Constants;

/**
 * This class is the actual BNLS server.  It creates the server socket,
 * and accepts connections on it, spawning a new thread for every connection.
 */
public class BNLSServer extends Thread{

	// Socket
	private ServerSocket server=null;
	private boolean listening=false;

	public BNLSServer() {
		Out.println("JBLS","Server thread created.");
	}

	//Starts the Server Listening process (infinatly blocking loop)
	public void run() {
		try
		{
		    server = new ServerSocket(Constants.BNLSPort);
		}catch (BindException e) {
			Out.error("JBLS", "Could not bind port " + Constants.BNLSPort + ". JBLS server is disabled.");
			return;
		}catch (IOException e){
		    Out.error("JBLS","Could not create socket (Port: " + Constants.BNLSPort + ")  - Shutting down JBLS. \r\n"+e.toString());
		    Controller.disableJBLSServer();
		    return;
		}
		listening=true;
		Out.println("JBLS","Server socket opened on port " + Constants.BNLSPort);

		while (listening) {
			try {
				Out.info("JBLS","Listening for new Connection...");
				Socket inSocket=server.accept();//block until a connection is made

				BNLSConnectionThread bConnection = new BNLSConnectionThread(inSocket); //Create the new thread
				if (Controller.lLinkedHead == null) {
			      Controller.lLinkedHead = bConnection;
				  bConnection.setNext(null);
				} else {
				  Controller.lLinkedHead.setPrev(bConnection);
				  bConnection.setNext(Controller.lLinkedHead);
				  Controller.lLinkedHead = bConnection;
				}
				bConnection.start();

			}catch (IOException e) {
				Out.error("JBLS","Could not Accept Connection " + e.toString());
				listening=false;
				break;
			}//end try-catch
		}//end listening loop
		Out.info("JBLS","Server Thread Terminated");
	}//end run method

	/**
	 * Closes the Server Socket to break it out of the loop
	 *
	 */
	public void closeSocket(){
		listening=false;
		try{
			if(!server.isClosed())
				server.close();
		}catch(IOException e){
			Out.error("JBLS","Error Closing Socket: "+e.toString());
		}
	}

	/** Destroy all current BNLS connections */
	public void destroyAllConnections() {
	    BNLSConnectionThread bCurrent = Controller.lLinkedHead;
	    BNLSConnectionThread bTemp;
		while (bCurrent != null) {
		   bCurrent.Destroy();
		   bTemp = bCurrent.getNext();
		   bCurrent = null;
		   bCurrent = bTemp;
		}
	}
}//end server class
