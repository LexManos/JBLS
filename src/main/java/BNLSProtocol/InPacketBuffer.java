package BNLSProtocol;

/*
 * Created on Sep 24, 2004
 *
 * This class stores info on an incoming packet, and provides info
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * Extension of Iago's buffer class.  Since I had to use it for the Hash/
 *  SRP returns, I decided I might as well make good use of it and extend
 *  it for my own use.
 *
 * Provides a place to store incoming packets
 * Provides methods for removing appropriate sections of the packet
 * @throws IndexOutOfBoundException When there is no more data left to return
 *
 */
import util.Buffer;
public class InPacketBuffer extends Buffer{
	private byte packetID;//packet id of the packet
	private short pLength;
	public static final long serialVersionUID=0x1234;


	public InPacketBuffer(byte pID, short pLen, String inData) throws InvalidPacketException {
		super(inData.getBytes());
		this.packetID=pID;
		this.pLength=pLen;
		//this.data=inData.getBytes();

	}//end of construction
	public InPacketBuffer(byte pID, short pLen) throws InvalidPacketException {
		//super(inData.getBytes());
		this.packetID=pID;
		this.pLength=pLen;
		//this.data=inData.getBytes();

	}//end of construction

	public byte getPacketID() {
		return packetID;
	}

}
