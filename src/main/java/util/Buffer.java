package util;

import java.io.Serializable;

/** Buffer.java
 * This class makes it easy for the programmer to create an array of bytes
 * of a specific format.  It'll support these major types:<BR>
 * DWord - 4 bytes<BR>
 * Word - 2 bytes<BR>
 * Byte - 1 byte<BR>
 * NTString - Any number of bytes, interpreted as characters, terminated by a null.<BR>
 * <P>
 * This is a pretty standard buffer in terms of everything else.  I've used it extensively
 * and have complete confidence in it.
 *
 * @author Ron
 */

public class Buffer implements Serializable
{
	public static final long serialVersionUID=0x1234;
    /** Whether or not we're debugging the buffer */
    public final boolean DEBUG = false;

    /** The starting length of the buffer */
    public final int defaultLength = 32;

    /** The actual buffer which will hold the data */
    protected char[] buffer;

    /** The current length of the buffer */
    private int currentLength;
    /** The maximum length of the buffer */
    private int maxLength;

    /** Initializes the variables */
    public Buffer()
    {
        buffer = new char[defaultLength];
        currentLength = 0;
        maxLength = defaultLength;
    }

    /** Initialize the buffer to an array of bytes.
     * @param   b   The initial value.
     */
    public Buffer(byte[] b)
    {
        this();
        addBytes(b);
    }

    /** Initializes the buffer to another buffer.  Note that it makes a copy, it doesn't share
     * the data.
     * @param   b   The original buffer.
     */
    public Buffer(Buffer b)
    {
        this();
        addChars(b.getBufferChar());
    }



    /** Returns the size of the buffer */
    public int size()
    {
        return currentLength;
    }

    /** Returns the entire buffer as an array of bytes.
     * @return  The entire buffer as an array of bytes.
     */
    public byte[] getBuffer()
    {
        byte[] ret = new byte[currentLength];
        for(int x = 0; x < currentLength; x++) ret[x] = (byte)buffer[x];
        //System.arraycopy(buffer, 0, ret, 0, currentLength);
        return ret;
    }
    public char[] getBufferChar(){
        char[] ret = new char[currentLength];
        System.arraycopy(buffer, 0, ret, 0, currentLength);
        return ret;
    }

    /** Ensures that there is enough length to store the specified number of bytes.  If there
     * isn't, enough extra room is allocated so we can.
     *
     * @param bytes The number of bytes we'return adding to it.
     */
    private void verifyLength(int bytes)
    {
        // If we already have enough, just return
        if((currentLength + bytes) <= maxLength)
            return;

        while((currentLength + bytes) > maxLength)
        {
            maxLength = maxLength * 2;
        }

        if(DEBUG)
            System.out.println("Increasing max length to " + maxLength);

        // Create a new buffer
        char[] newBuffer = new char[maxLength];

        // Copy the old buffer into the new buffer
        System.arraycopy(buffer, 0, newBuffer, 0, currentLength);

        // Set the current buffer to the new buffer
        buffer = newBuffer;
    }

    /** Shifts all data back by the requested number of bytes, and returns the
     * bytes that were shifted off.
     *
     * @param number    The number of bytes to pull off the beginning.
     * @return          The bytes that were pulled off.
     */
    protected char[] remove(int number)
    {
        char[] ret = new char[number];
        System.arraycopy(buffer, 0, ret, 0, number);
        System.arraycopy(buffer, number, buffer, 0, currentLength - number);
        currentLength -= number;
        return ret;
    }

    /** Adds a single byte to the end of the buffer .
     * @param b     The byte to add.
     */
    public void addByte(byte b)
    {
        verifyLength(1);
        buffer[currentLength++] = (char)b;
    }
    public void addChar(char b)
    {
        verifyLength(1);
        buffer[currentLength++] = (char)b;
    }

    /** Removes a single byte from the beginning of the buffer.
     * @return The byte that was removed.
     * @throws IndexOutOfBoundsException If there isn't enough room in the buffer to accomidate the
     *         requested removal.
     */
    public byte removeByte() throws IndexOutOfBoundsException
    {
        if(currentLength == 0)
            throw new IndexOutOfBoundsException("Attempted to remove data from the buffer that wasn't there.");
        return (byte)(remove(1)[0]);
    }
    public char removeChar() throws IndexOutOfBoundsException
    {
        if(currentLength == 0)
            throw new IndexOutOfBoundsException("Attempted to remove data from the buffer that wasn't there.");
        return remove(1)[0];
    }

    /** Returns the byte at a specific location.
     * @param index The location to get the byte at.
     * @return      The byte at location "index".
     */
    public byte byteAt(int index)
    {
        return (byte)buffer[index];
    }

    /** Adds a word to the buffer (2 bytes, little endian).
     * @param w The word to add.
     */
    public void addWord(short w)
    {
        addByte((byte)((w & 0x00FF) >> 0));
        addByte((byte)((w & 0xFF00) >> 8));
    }

    /** Removes and returns a single word (2 bytes).
     * @return  The word that was removed.
     * @throws IndexOutOfBoundsException If there isn't enough room in the buffer to accomidate the
     *         requested removal.
     */
    public short removeWord() throws IndexOutOfBoundsException
    {
        int ret = ((removeByte() << 0) & 0x00FF) |
                  ((removeByte() << 8) & 0xFF00);

        return (short)(ret & 0x0000FFFF);
    }

    /** Adds a dword to the buffer (4 bytes, little endian).
     * @param d The dword to add.
     */
    public void addDWord(int d)
    {
        addByte((byte)((d & 0x000000FF) >> 0));
        addByte((byte)((d & 0x0000FF00) >> 8));
        addByte((byte)((d & 0x00FF0000) >> 16));
        addByte((byte)((d & 0xFF000000) >> 24));
    }
    public void addDWord(boolean d)
    {
    	if (d) {
    		addByte((byte)0x01);
    	}else{
    		addByte((byte)0x00);
    	}
        addByte((byte)0x00);
        addByte((byte)0x00);
        addByte((byte)0x00);
    }

    public void addArray(int []a)
    {
        for(int i = 0; i < a.length; i++)
            addDWord(a[i]);
    }

    /** Removes and returns a single dword (4 bytes).
     * @return The DWord that was removed.
     * @throws IndexOutOfBoundsException If there isn't enough room in the buffer to accomidate the
     *         requested removal.
     */
    public int removeDWord() throws IndexOutOfBoundsException
    {
        return ((removeByte() << 0) & 0x000000FF) |
               ((removeByte() << 8) & 0x0000FF00) |
               ((removeByte() << 16) & 0x00FF0000) |
               ((removeByte() << 24) & 0xFF000000);
    }

    /** Adds a QWord to the buffer (8 bytes, little endian)
     * @param   l   The value to add.
     */
    public void addLong(long l)
    {
        addByte((byte)((l & 0x00000000000000FFl) >> 0l));
        addByte((byte)((l & 0x000000000000FF00l) >> 8l));
        addByte((byte)((l & 0x0000000000FF0000l) >> 16l));
        addByte((byte)((l & 0x00000000FF000000l) >> 24l));
        addByte((byte)((l & 0x000000FF00000000l) >> 32l));
        addByte((byte)((l & 0x0000FF0000000000l) >> 40l));
        addByte((byte)((l & 0x00FF000000000000l) >> 48l));
        addByte((byte)((l & 0xFF00000000000000l) >> 56l));

    }

    /** Removes and returns a single dword (8 bytes).
     * @return The long at the beginning of the buffer.
     * @throws IndexOutOfBoundsException If there isn't enough room in the buffer to accomidate the
     *         requested removal.
     */
    public long removeLong() throws IndexOutOfBoundsException
    {
        return (((long)removeByte() <<  0L) & 0x00000000000000FFL) |
               (((long)removeByte() <<  8L) & 0x000000000000FF00L) |
               (((long)removeByte() << 16L) & 0x0000000000FF0000L) |
               (((long)removeByte() << 24L) & 0x00000000FF000000L) |
               (((long)removeByte() << 32L) & 0x000000FF00000000L) |
               (((long)removeByte() << 40L) & 0x0000FF0000000000L) |
               (((long)removeByte() << 48L) & 0x00FF000000000000L) |
               (((long)removeByte() << 56L) & 0xFF00000000000000L);
    }

    /** Adds a non-null-terminated string to the buffer
     * @param s The string to add to the buffer.
     */
    public void addString(String s)
    {
        for(int i = 0; i < s.length(); i++)
        {
            addChar((char) s.charAt(i));
        }
    }

    /** Removes 'i' bytes from the buffer, and returns them as a string
     * @param i The number of bytes to remove.
     * @return The bytes removed as a String.
     * @throws IndexOutOfBoundsException If there isn't enough room in the buffer to accomidate the
     *         requested removal.
     */
    public String removeString(int i) throws IndexOutOfBoundsException
    {
        StringBuffer s = new StringBuffer(i + 1);

        for(int j = 0; j < i; j++)
        {
            s.append((char) removeByte());
        }

        return s.toString();
    }

    /** Adds a null-terminated string to the buffer, including the null
     * @param s The string to add, without the null terminator.
     */
    public void addNTString(String s)
    {
        addString(s);
        addByte((byte) 0x00);
    }

    /** Removes and returns a null-terminated string, without the null
     * @return  The first null-terminatred string in the buffer, without the null.
     * @throws IndexOutOfBoundsException If the buffer ended before finding a Null.
     */
    public String removeNTString()
    {
        StringBuffer s = new StringBuffer();

        char b = removeChar();

        while(b != (char)0x00)
        {
            s.append(b);
            b = removeChar();
        }

        return s.toString();
    }

    /** Adds an array of bytes to the buffer
     * @param b The bytes to add to the buffer.
     */
    public void addBytes(byte[] b)
    {
        for(int i = 0; i < b.length; i++)
            addByte(b[i]);
    }
    public void addChars(char[] b)
    {
        for(int i = 0; i < b.length; i++)
            addChar(b[i]);
    }

    /** Removes 'i' bytes from the array and returns them as an array of bytes.
     * @param i The number of bytes to remove.
     * @return  The bytes that were removed.
     * @throws IndexOutOfBoundsException If there isn't enough room in the buffer to accomidate the
     *         requested removal.
     */
    public byte[] removeBytes(int i) throws IndexOutOfBoundsException{
        byte[] ret = new byte[i];
        for(int x=0;x<i;x++) ret[x] = (byte)(remove(1)[0]);
        return ret;
    }

    /** Adds another buffer to the end of the buffer.  All it actually does is add the bytes
     * of the source to the current buffer.
     * @param b The buffer to add.
     */
    public void addBuffer(Buffer b)
    {
        addBytes(b.getBuffer());
    }

    /** Quicly add a byte to the buffer.
     * @param b The byte to add.
     */
    public void add(byte b)
    {
        addByte(b);
    }
    public void add(char b)
    {
        addChar(b);
    }

    /** Quickly add a short to the buffer.
     * @param s The short to add.
     */
    public void add(short s)
    {
        addWord(s);
    }

    /** Quickly add an int to the buffer.
     * @param i The inteter to add.
     */
    public void add(int i)
    {
        addDWord(i);
    }

    public void add(int []i)
    {
        addArray(i);
    }

    /** Quickly add a long to the buffer.
     * @param l The long to add.
     */
    public void add(long l)
    {
        addLong(l);
    }

    /** Quickly add an array of bytes to the buffer.
     * @param b The array of bytes.
     */
    public void add(byte[] b)
    {
        addBytes(b);
    }

    /** Quickly append another buffer to the current buffer.
     * @param b The source buffer.
     */
    public void add(Buffer b)
    {
        addBuffer(b);
    }

    /** Gets the buffer, formatted in a pretty way.
     * @return The formatted string.  It might look something like this:<BR>
     *<PRE>
     *      00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F    ................
     *      69 68 67 66 65 64 63 62 61 61 6A 6B 6C 6D 6E 00    ihgfedcbaajklmn.
     *      41 00                                              A.
     *      Length: 34
     *</PRE>
     */
    public String toString()
    {
        StringBuffer returnString = new StringBuffer( (currentLength * 3) + // The hex
                                                      (currentLength) +     // The ascii
                                                      (currentLength / 4) + // The tabs/\n's
                                                      30 );                 // The text

        //returnString.append("Buffer contents:\n");
        int i, j; // Loop variables
        for(i = 0; i < currentLength; i++)
        {
            if((i != 0) && (i % 16 == 0))
            {
                // If it's a multiple of 16 and i isn't null, show the ascii
                returnString.append('\t');
                for(j = i - 16; j < i; j++)
                {
                    if(buffer[j] < 0x20 || buffer[j] > 0x7F)
                        returnString.append('.');
                    else
                        returnString.append((char)buffer[j]);
                }
                // Add a linefeed after the string
                returnString.append("\n");
            }

            returnString.append(Integer.toString((buffer[i] & 0xF0) >> 4, 16) +
                    Integer.toString((buffer[i] & 0x0F) >> 0, 16));
            returnString.append(' ');
        }

        // Add padding spaces if it's not a multiple of 16
        if(i != 0 && i % 16 != 0)
        {
            for(j = 0; j < ((16 - (i % 16)) * 3); j++)
            {
                returnString.append(' ');
            }
        }
        // Add the tab for alignment
        returnString.append('\t');

        // Add final chararacters at right, after padding

        // If it was at the end of a line, print out the full line
        if(i > 0 && (i % 16) == 0)
        {
            j = i - 16;
        }
        else
        {
            j = (i - (i % 16));
        }

        for(; i >= 0 && j < i; j++)
        {
            if(buffer[j] < 0x20 || buffer[j] > 0x7F)
                returnString.append('.');
            else
                returnString.append((char) buffer[j]);
        }

        // Finally, tidy it all up with a newline
        returnString.append('\n');
        returnString.append("Length: " + currentLength + '\n');

        return returnString.toString();
    }
}
