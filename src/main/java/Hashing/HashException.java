/*
 * Created on Sep 29, 2004
 *
 */
package Hashing;

/**
 * Generic Exception Class.  Stores a description string.
 * Thrown when a hashing error has occured
 */
public class HashException extends Exception {
	private String errorDes;
	public static final long serialVersionUID=0x1234;

	public HashException(String er) {
		errorDes=er;
	}

	public String getError() {
		return errorDes;
	}

	public String toString() {
		return "Error hashing: "+errorDes + super.toString();
	}

}
