/**
 * Created on Sep 19, 2004
 */

package BNLSProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import util.Constants;
import util.Controller;
import util.Out;
import BNLSProtocol.BNLSConnectionThread;

/**
 *
 * Individual thread to accept data on a BNLS Connection Seperates out the
 * packets and contains its own Parser class for interpretting them
 */
public class BNLSConnectionThread extends Thread
{
    /** Total Connection Count * */
    public static int connectionCount = 0;

    /** Next item in the linked list */
    private BNLSConnectionThread bNextList = null;
    private BNLSConnectionThread bPrevList = null;

    /** Thread's Socket */
    private Socket socket = null;

    private OutputStream out = null;
    public InetAddress address = null;
	public String IP = null;

    /*
     * Note this is only an InputStream. I spent hours trying to figure out what
     * was wrong with the code when all along it was that "InputStreamReader"
     * was changing some values.
     */
    private InputStream in = null;

    /** Current Thread Count */
    private static int threadCount = 0;

    /** Thread ID of this instance */
    public int threadID;


    /** Set the next item in the linked list */
    public void setNext(BNLSConnectionThread bNext){
    	bNextList = bNext;
    }
    /** Get the next item in the linked list */
    public BNLSConnectionThread getNext(){
    	return bNextList;
    }
    /** Set the last item in the linked list */
    public void setPrev(BNLSConnectionThread bPrev){
    	bPrevList = bPrev;
    }
    /** Get the Last item in the linked list */
    public BNLSConnectionThread getPrev(){
    	return bPrevList;
    }

    /** Destry removed this thread from the Linked List. */
    public void Destroy() {
    	if (bPrevList == null) {
    		if (bNextList == null )
    		  Controller.lLinkedHead = null;
    		else
    	      Controller.lLinkedHead = bNextList;
    	} else {
    	  if (bNextList == null) {
    	    bPrevList.setNext(null);
    	  } else {
    	    bPrevList.setNext(bNextList);
    	    bNextList.setPrev(bPrevList);
    	  }
    	}
    	try {
    	  out.close();
          in.close();
          socket.close();
        }catch (IOException e){
            e.printStackTrace();
            Out.error("Thread " + threadID, "IO Error:" + e.toString());
        }
    }



    /** Creates the Interpreter Thread with a given socket */
    public BNLSConnectionThread(Socket cSocket)
    {
        super("BNLSConnectionThread");
        threadID = threadCount++;
        connectionCount++;
        socket = cSocket;
        setDaemon(true);// make this Thread Not Hold up the Program
    }
    /** Runs the Connection thread(blocks until connection done) */
    public void run()
    { // Run the connection thread

        // Check for too many thread instances(dont want to overload server)
        if (threadCount > Constants.maxThreads)
        {
            Out.error("JBLS", "Max Threads Exceeded. Current count: " + threadCount + ". Max count: " + Constants.maxThreads + ".  Connection terminated.");
            threadCount--;
            Destroy();
            return;
        }
        this.address = socket.getInetAddress();
        this.IP = this.address.getHostAddress();

        // Check for IPStatistics for this guy
        if (!IpAuth.checkAuth(IP))
        {
            Out.error("Thread " + threadID, "IP Not Authorized.  Thread Terminated.");
            threadCount--;
            Destroy();
            return;
        }

		int[] ipHash = Hashing.BrokenSHA1.calcHashBuffer(this.IP.getBytes());
		this.IP = util.PadString.padHex(ipHash[0], 8) + 
		          util.PadString.padHex(ipHash[1], 8) + 
		          util.PadString.padHex(ipHash[2], 8) + 
		          util.PadString.padHex(ipHash[3], 8) +
		          util.PadString.padHex(ipHash[4], 8);
	    if(Controller.stats != null) Controller.stats.onConnection(this.IP);
		
        OutPacketBuffer outputLine;
        BNLSParse myParse = new BNLSParse(this);// create BNLS Parsing Class
        boolean parsing = true;
        byte errCount = 0;

        try
        {
            // Retrieve Input and output Streams
            out = socket.getOutputStream();
            in = socket.getInputStream();
            socket.setSoTimeout(60000);// 60 second timeout
            socket.setKeepAlive(true);// keep connection alive

            Out.debug("Thread " + threadID, "Streams created.");

            while (parsing){
                outputLine = null;
                try
                {
                    int i;// input integer(read from input string)
                    short pLength;// packet Length
                    byte packetID;
                    i = in.read();// read in first byte of Packet Length
                    if (i == -1) throw new IOException("Connection terminated.");
                    pLength = (short) ((i << 0) & 0x000000FF);
                    i = in.read();// Second Packet Length Byte
                    if (i == -1) throw new IOException("Connection terminated.");
                    pLength |= (short) ((i << 8) & 0x0000FF00);
                    packetID = (byte) in.read();// Read in PacketID

                    InPacketBuffer inPacket = new InPacketBuffer(packetID, pLength);
                    int bytesRead = 0;
                    while (bytesRead < pLength - 3)
                    {// read in each byte
                        i = in.read();
                        if (i == -1)
                            throw new IOException("Connection terminated.");
                        inPacket.add((char)i);// add to packet
                        bytesRead++;
                    }
                    if (Constants.displayPacketInfo || Constants.displayParseInfo)
                        Out.info("Thread " + threadID, "Input Received. Packet ID: 0x" + ((packetID & 0xF0) >> 4) + "" + Integer.toString((packetID & 0x0F) >> 0, 16) + " Length: " + pLength + ".");
                    if (Constants.debugInfo && packetID == 0)
					    Out.debug("Thread " + threadID, "0x00 received from  " + IP);

                    outputLine = myParse.parseInput(inPacket);
                    if (outputLine != null)
                    {
                        if (Constants.displayPacketInfo)
                            Out.info("Thread " + threadID, "Sending response.");
                        out.write(outputLine.getBuffer());
                        outputLine = null;
                    }else{// outputline=null, no response
                        if (Constants.displayPacketInfo)
                            Out.info("Thread " + threadID, "No response.");// +outputLine.toString());
                    }
                }catch (InvalidPacketException e){
                    Out.error("Thread " + threadID, "Invalid Packet: " + e.toString());
                    errCount++;
                    if (errCount > 2) break;
                }catch (InterruptedIOException e){
                    Out.error("Thread " + threadID, "Connection Timeout");
                    parsing = false;
                    break;
                }catch (IOException e){
                    Out.info("Thread " + threadID, "Disconnected (" + e.getMessage() + ")");
                    parsing = false;
                    break;
                }catch (BNLSException e){ // Fatal BNLS Error(Not authorized, etc.)
                    Out.error("Thread " + threadID, "BNLS Exception: " + e.toString());
                    parsing = false;
                    break;
                }// end inner Try-Catch
            }// end input while loop

            // Take Care of Streams/Sockets
            out.close();
            in.close();
            socket.close();

        }catch (IOException e){
            e.printStackTrace();
            Out.error("Thread " + threadID, "IO Error:" + e.toString());
        }

        Out.debug("Thread " + threadID, "Closed");
        threadCount--;
        Destroy();

    }// end of run method
    public boolean send(OutPacketBuffer data){
      try{
        out.write(data.getBuffer());
      }catch(Exception e){ return false;}
      return true;
    }
}
