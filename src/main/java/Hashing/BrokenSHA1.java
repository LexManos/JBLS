package Hashing;

import util.ByteFromIntArray;

/** This is an implementation of the Broken SHA1 hashing that battle.net uses for
 * various packets (including passwords, cdkey, etc).  It hashes the passed in
 * data down to 5 bytes (160 bits).  It uses an algorithm that is very similar to the
 * standard SHA-1 hashing, except is a little different.
 * <P>
 * Part of the credit for this goes to Yobguls - this is based off his code.
 * @author iago
 */
public class BrokenSHA1
{
    /** Calculates the 20 byte hash based on the passed in byte[] data.
     * @param hashData The data to hash.
     * @return The 20 bytes of hashed data.  Note that this array is actually 60 bytes long,
     * but the last 40 bytes should just be ignored.
     */
    public static int[] calcHashBuffer(byte[] hashData)
    {
        // Allocate the memory to return the 20 bytes
        //int[] ret = new int[5];

        // Allocate enough room for the 0x40 bytes and the 5 starting bytes
        int[] hashBuffer = new int[0x10 + 5];

        // Fill in the default values
        hashBuffer[0] = 0x67452301;
        hashBuffer[1] = 0xEFCDAB89;
        hashBuffer[2] = 0x98BADCFE;
        hashBuffer[3] = 0x10325476;
        hashBuffer[4] = 0xC3D2E1F0;

        for(int i = 0; i < hashData.length; i += 0x40)
        {
            // Length of this subsection
            int subLength = hashData.length - i;

            // subLength can't be more than 0x40
            if(subLength > 0x40)
                subLength = 0x40;

            // Copy this part of the hashdata into the int array
            ByteFromIntArray bfia = new ByteFromIntArray(true);
            for(int j = 0; j < subLength; j++){
                bfia.insertByte(hashBuffer, j + (4 * 5), hashData[j + i]);
            }

            // If we don't reach the end of the buffer, pad it
            if(subLength < 0x40){
                //byte padNumber = (byte)(0x40 - subLength);

                for(int j = subLength; j < 0x40; j++)
                    bfia.insertByte(hashBuffer, j + (4 * 5), (byte) 0);
            }

            doHash(hashBuffer);
        }

        return hashBuffer;
    }

    /** Hashes the next 0x40 bytes of the int.
     * @param hashBuffer The current 0x40 bytes we're hashing.
     */
    private static void doHash(int[] hashBuffer)
    {
        int buf[] = new int[0x50];
        int dw, a, b, c, d, e;
        int p;

        int i;

        for(i = 0; i < 0x10 ; i++)
            buf[i] = hashBuffer[i + 5];


        for(i = 0x10; i < 0x50; i++)
        {
            dw = buf[i-0x10] ^ buf[i-0x8] ^ buf[i-0xE] ^ buf[i-0x3];
            buf[i] = (1 >>> (0x20 - (byte)dw)) | (1 << (byte) dw);
        }

        a = hashBuffer[0];
        b = hashBuffer[1];
        c = hashBuffer[2];
        d = hashBuffer[3];
        e = hashBuffer[4];

        p = 0;

        i = 0x14;
        do
        {
            dw = ((a << 5) | (a >>> 0x1b)) + ((~b & d) | (c & b)) + e + buf[p++] + 0x5a827999;
            e = d;
            d = c;
            c = (b >>> 2) | (b << 0x1e);
            b = a;
            a = dw;
        }
        while(--i > 0);

        i = 0x14;
        do
        {
            dw = (d ^ c ^ b) + e + ((a << 5) | (a >>> 0x1b)) + buf[p++] + 0x6ED9EBA1;
            e = d;
            d = c;
            c = (b >>> 2) | (b << 0x1e);
            b = a;
            a = dw;
        }
        while(--i > 0);

        i = 0x14;
        do
        {
            dw = ((c & b) | (d & c) | (d & b)) + e + ((a << 5) | (a >>> 0x1b)) + buf[p++] - 0x70E44324;
            e = d;
            d = c;
            c = (b >>> 2) | (b << 0x1e);
            b = a;
            a = dw;
        }
        while(--i > 0);

        i = 0x14;
        do
        {
            dw = ((a << 5) | (a >>> 0x1b)) + e + (d ^ c ^ b) + buf[p++] - 0x359D3E2A;
            e = d;
            d = c;
            c = (b >>> 2) | (b << 0x1e);
            b = a;
            a = dw;
        }
        while(--i > 0);

        hashBuffer[0] += a;
        hashBuffer[1] += b;
        hashBuffer[2] += c;
        hashBuffer[3] += d;
        hashBuffer[4] += e;
    }
}
