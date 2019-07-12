/*
 * DoubleHash.java
 *
 * Created on April 13, 2004, 12:55 PM
 */

package Hashing;

import util.Buffer;

/** This class does the double-hash for passwords.  It hashes them alone, then it
 * hashes them again along with the client and server tokens.
 */
public class DoubleHash
{
    /** This static method does the actual doublehash.
     * @param str The string we're doublehashing.
     * @param clientToken The client token for this session.
     * @param serverToken The server token for this session.
     * @return The 5-DWord (20 byte) hash.
     */
    static public int[] doubleHash(String str, int clientToken, int serverToken)
    {
            Buffer initialHash = new Buffer();
            initialHash.addNTString(str);
            int[] hash1 = BrokenSHA1.calcHashBuffer(initialHash.getBuffer());

            Buffer secondHash = new Buffer();
            secondHash.add(clientToken);
            secondHash.add(serverToken);
            for(int i = 0; i < 5; i++)
                secondHash.add(hash1[i]);

            return BrokenSHA1.calcHashBuffer(secondHash.getBuffer());
    }
}
