/*
 * Created on Oct 3, 2004
 */
package BNLSProtocol;

/**
 *
 * Generic Exception Class.  Stores a description string.
 * Thrown when the BNLS Parse has encountered a problem.
 * Acts as a generic Message to terminate the current connection
 * Ex: couldn't validate BNLS Username/Pass, etc.
 */

public class BNLSException extends Exception {
	private String errorDes;
	public static final long serialVersionUID=0x1234;

	public BNLSException(String er) {
		errorDes=er;
	}

	public String getError() {
		return errorDes;
	}

	public String toString() {
		return "BNLS Fault: "+errorDes + super.toString();
	}

}
