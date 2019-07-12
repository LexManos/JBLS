/*
 * Created on Sep 24, 2004
 */
package BNLSProtocol;

/**
 * Generic Exception Class, stores a string.
 * Used to signal when an Invalid BNLS Packet Was Recieved.
 *
 *
 */
public class InvalidPacketException extends Exception {
	private String errorDes;
	public static final long serialVersionUID=0x1234;

	public InvalidPacketException(String er) {
		errorDes=er;
	}

	public String toString() {
		return "Invalid Packet Error: "+errorDes + super.toString();
	}

}
