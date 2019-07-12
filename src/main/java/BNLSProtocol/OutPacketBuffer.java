package BNLSProtocol;
/*
 * Created on Sep 24, 2004
 */

/**
 *  Extension of Iago's buffer class.  Since I had to use it for the Hash/
 *  SRP returns, I decided I might as well make good use of it and extend
 *  it for my own use.
 */
import util.Buffer;

public class OutPacketBuffer extends Buffer{
	public static final long serialVersionUID=0x1234;
	private byte packetID;


	public OutPacketBuffer(){
		super();
	}

	public OutPacketBuffer(byte ID){
		packetID=ID;
	}

	public OutPacketBuffer(int ID){
		packetID=(byte)ID;
	}

	/**
	 * Gets the Buffer in the form of a string
	 * Unused currently
	 */
	public String getStringBuffer(){//Unused?
		//int s=size();
		//return (char)(byte)((s+3 >> 0) & 0x000000FF) + (char)(byte)((s+3 >> 8) & 0x000000FF) + String.valueOf(super.getBuffer());
			StringBuffer s=new StringBuffer();
			int len=size();
	        s.append((char)(byte) ((len+3 >> 0) & 0x000000FF));//packet length
	        s.append((char)(byte) ((len+3 >> 8) & 0x000000FF));
	        s.append((char)packetID);//packetid
	        byte[] ret=super.getBuffer();
	        for(int x=0;x<ret.length;x++){//have to loop through to convert everything to chars
	        	s.append((char)ret[x]);
	        }
	        return s.toString();
	}

	/**
	 * Overrides the "Buffer" method, returning the packet ID/length
	 *  and the data
	 *
	 * @return byte[] of bytes to pass directly to output stream
	 */
	public byte[] getBuffer(){
       	byte[] ret = new byte[size()+3];

        System.arraycopy(super.getBuffer(), 0, ret, 3, size());//Copy Buffer Into a New Array

        ret[0] = (byte) ((ret.length >> 0) & 0x000000FF);
        ret[1] = (byte) ((ret.length >> 8) & 0x000000FF);
        ret[2] = (byte) packetID;

        //System.out.println("Ret Size"+ret.length + "last val"+ret[ret.length-1]);
        return ret;
    }

	public char[] getCharBuffer(){
		byte[] t=super.getBuffer();
		char[] ret=new char[t.length+3];
		ret[2] = (char)(byte) packetID;
        ret[0] = (char)(byte) ((ret.length >> 0) & 0x000000FF);
        ret[1] = (char)(byte) ((ret.length >> 8) & 0x000000FF);
		for(int x=3;x<ret.length;x++)
			ret[x]=(char)t[x-3];
		return ret;
	}

}
