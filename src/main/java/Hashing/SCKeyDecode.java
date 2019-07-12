/*
 * CDKeyDecode.java
 *
 * Created on March 17, 2004, 8:14 AM
 */

package Hashing;
import util.Buffer;

/** This decodes a standard numeric CDKey (Starcraft's) for sending to battle.net. */
public class SCKeyDecode
{
    /** The decoded cdkey. */    
    protected String cdkey;

    /** Does nothing - required for subclasses. */    
    public SCKeyDecode(){}
    
    /** Sets up the decoder with the specified key and goes ahead and decodes it.
     * @param cdkey The CDKey to hash.
     * @throws HashException If the cdkey is invalid.
     */    
    public SCKeyDecode(String cdkey) throws HashException
    {
        this.cdkey = cdkey;
        if(cdkey == null)
            throw new HashException("CDKey missing.");
        
        if(cdkey.length() != 13)
            throw new HashException("CDKey is invalid length");
        
        if(verify() == false)
            throw new HashException("CDKey is invalid.");
        
        getFinalValue();
    }
    
    /** Hashes the CDKey based on the client and server token, and returns the 5-byte
     * hash.
     * @param clientToken The client token to hash it with.
     * @param serverToken The server token to hash it with.
     * @return The 20-byte hash (5 ints).
     */    
    public int[] getKeyHash(int clientToken, int serverToken)
    {
        
        Buffer hashData = new Buffer();
        
        hashData.addDWord(clientToken);
        hashData.addDWord(serverToken);
        hashData.addDWord(getProduct());
        hashData.addDWord(getVal1());
        hashData.addDWord(0);
        hashData.addDWord(getVal2());
        
        return BrokenSHA1.calcHashBuffer(hashData.getBuffer());
    }
    
    
    /** Verifies that the CDKey is valid.
     * @return true if the CDKey is valid.
     * false if the CDKey is invalid.
     */    
    protected boolean verify()
    {
        int accum = 3;
        cdkey = cdkey.toLowerCase();
        
        for(int i = 0; i < (cdkey.length() - 1); i++)
            accum += ((cdkey.charAt(i) - '0') ^ (accum * 2));
            
        return ((accum % 10) == (cdkey.charAt(12) - '0'));
    }


    /** Gets the final CDKey values. */    
    protected void getFinalValue() {
		int c; 
		int hashkey = 0x13AC9741;
		int seq[] = {6, 0, 2, 9, 3, 11, 1, 7, 5, 4, 10, 8};
		char[] key = new char[12]; 
		for (short i = 11; i >= 0; i--) {
			c = cdkey.charAt(seq[i]);
			if (c <= '7') {
				c ^= (byte) (hashkey & 7);
				hashkey >>>= 3;
			} else c ^= (byte)(i & 1);
			key[i] = (char)c;
		}
		cdkey = new String(key);
	}

    /** Gets the game's product.
     * @return The game's product.
     */    
    public int getProduct() {
        return Integer.parseInt(cdkey.substring(0, 2));
    }
    /** Gets the second CDKey value.
     * @return The second CDKey value.
     */    
    public int getVal2() {
        return Integer.parseInt(cdkey.substring(9, 12));
    }
    /** Gets the first CDKey value.
     * @return The first CDKey value.
     */    
    public int getVal1() {
        return Integer.parseInt(cdkey.substring(2, 9));
    }
}