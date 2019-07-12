/*
 * AlphaKeyDecode.java
 *
 * Created on April 5, 2004, 3:34 PM
 */

package Hashing;


/**
 * This is the CDKey Decoder used for decoding alphabetic keys - Warcraft 2,
 * Diablo 2, etc.
 * 
 */
public class D2KeyDecode extends SCKeyDecode
{
    int[] alphaMap = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, 0x00, -1, 0x01, -1, 0x02, 0x03, 0x04, 0x05, -1, -1, -1, -1, -1, -1, -1, -1, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, -1, 0x0D, 0x0E, -1, 0x0F, 0x10, -1, 0x11, -1,
            0x12, -1, 0x13, -1, 0x14, 0x15, 0x16, -1, 0x17, -1, -1, -1, -1, -1, -1, -1, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, -1, 0x0D, 0x0E, -1, 0x0F, 0x10, -1, 0x11, -1, 0x12, -1, 0x13, -1,
            0x14, 0x15, 0x16, -1, 0x17, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };

    private int checksum = 0;

    public D2KeyDecode(String cdkey) throws HashException
    {
        this.cdkey = cdkey;

        if (cdkey == null)
            throw new HashException("CDKey missing.");

        if (cdkey.length() != 16)
            throw new HashException("CDKey is invalid length");

        hash();
    }

    private void hash() throws HashException
    {
        checksum = 0;
        char[] keyarray = cdkey.toCharArray();
        int r, n, n2, v, v2;
        byte c1, c2, c;

        for (int i = 0; i < cdkey.length(); i += 2)
        {
            r = 1;
            c1 = (byte) alphaMap[cdkey.charAt(i)];
            n = c1 * 3;
            c2 = (byte) alphaMap[cdkey.charAt(i + 1)];
            n = c2 + (n * 8);

            if (n >= 0x100)
            {
                n -= 0x100;
                checksum = checksum | (int) Math.pow(2, (i / 2));
            }
            n2 = n;
            n2 >>= 4;
            keyarray[i] = getHexVal(n2);
            keyarray[i + 1] = getHexVal(n);
            r <<= 1;
        }

        v = 3;
        for (int i = 0; i < 16; i++)
        {
            c = (byte) keyarray[i];
            n = getNumVal((char) c);
            n2 = v * 2;
            n ^= n2;
            v += n;
        }

        v &= 0xFF;
        if (v != checksum)
            throw new HashException("CDKey is invalid.");

        for (int i = 15; i >= 0; i--)
        {
            c = (byte) keyarray[i];
            if (i > 8)
                n = i - 9;
            else
                n = 0xF - (8 - i);
            n &= 0xF;
            c2 = (byte) keyarray[n];
            keyarray[i] = (char) c2;
            keyarray[n] = (char) c;
        }
        v2 = 0x13AC9741;

        for (int i = 15; i >= 0; i--)
        {
            c = (byte) Character.toUpperCase(keyarray[i]);
            keyarray[i] = (char) c;
            if (c <= '7')
            {
                v = v2;
                c2 = (byte) (v & 0xFF);
                c2 &= 7;
                c2 ^= c;
                v >>= 3;
                keyarray[i] = (char) c2;
                v2 = v;
            }
            else if (c < 'A')
            {
                c2 = (byte) i;
                c2 &= 1;
                c2 ^= c;
                keyarray[i] = (char) c2;
            }
        }

        cdkey = new String(keyarray);
    }

    private char getHexVal(int v)
    {
        v &= 0xF;
        if (v < 10)
            return (char) (v + 0x30);
        else
            return (char) (v + 0x37);
    }

    private int getNumVal(char c)
    {
        c = Character.toUpperCase(c);
        if (Character.isDigit(c))
            return (c - 0x30);
        else
            return (c - 0x37);
    }

    public int getProduct()
    {
        return Integer.parseInt(cdkey.substring(0, 2), 16);
    }

    public int getVal1()
    {
        return (int) Long.parseLong(cdkey.substring(2, 8), 16);
    }

    public int getVal2()
    {
        return (int) Long.parseLong(cdkey.substring(8, 16), 16);
    }

}