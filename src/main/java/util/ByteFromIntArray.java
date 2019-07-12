/*
 * ByteFromIntArray.java
 *
 * Created on May 21, 2004, 11:39 AM
 */

package util;

/** This is a class to take care of treating an array of ints like a an array of bytes.
 * Note that this always works in Little Endian
 */
public class ByteFromIntArray
{
    private boolean littleEndian;
    
    public static final ByteFromIntArray LITTLEENDIAN = new ByteFromIntArray(true);
    public static final ByteFromIntArray BIGENDIAN = new ByteFromIntArray(false);

    /**
     * @param args the command line arguments
     */
    /*public static void main(String[] args)
    {
        int []test = { 0x01234567, 0x89abcdef };
        
        ByteFromIntArray bfia = new ByteFromIntArray(false);
        
        byte []newArray = bfia.getByteArray(test);
        
        for(int i = 0; i < newArray.length; i++)
            System.out.print(" " + PadString.padHex(newArray[i], 2));
    }*/
    
    public ByteFromIntArray(boolean littleEndian)
    {
        this.littleEndian = littleEndian;
    }

    public byte getByte(int[] array, int location)
    {
        if((location / 4) >= array.length)
            throw new ArrayIndexOutOfBoundsException("location = " + location + ", number of bytes = " + (array.length * 4));
        
        int theInt = location / 4; // rounded
        int theByte = location % 4; // remainder
        
        
        // reverse the byte to simulate little endian
        if(littleEndian)
            theByte = 3 - theByte;
        
        // I was worried about sign-extension here, but then I realized that they are being
        // put into a byte anyway so it wouldn't matter.
        if(theByte == 0)
            return (byte)((array[theInt] & 0x000000FF) >> 0);
        else if(theByte == 1)
            return (byte)((array[theInt] & 0x0000FF00) >> 8);
        else if(theByte == 2)
            return (byte)((array[theInt] & 0x00FF0000) >> 16);
        else if(theByte == 3)
            return (byte)((array[theInt] & 0xFF000000) >> 24);
        
        return 0;
    }
    
    
    /** This function is used to insert the byte into a specified spot in
     * an int array.  This is used to simulate pointers used in C++.
     * Note that this works in little endian only.
     * @param intBuffer The buffer to insert the int into.
     * @param b The byte we're inserting.
     * @param location The location (which byte) we're inserting it into.
     * @return The new array - this is returned for convenience only.
     */
    public int[] insertByte(int[] intBuffer, int location, byte b)
    {
        // Get the location in the array and in the int
        int theInt = location / 4;
        int theByte = location % 4;

        // If we're using little endian reverse the hex position
        if(littleEndian == false)
            theByte = 3 - theByte;
        
        int replaceInt = intBuffer[theInt];
        
        // Creating a new variable here because b is a byte and I need an int
        int newByte = b << (8 * theByte);

        if(theByte == 0)
            replaceInt &= 0xFFFFFF00;
        else if(theByte == 1)
            replaceInt &= 0xFFFF00FF;
        else if(theByte == 2)
            replaceInt &= 0xFF00FFFF;
        else if(theByte == 3)
            replaceInt &= 0x00FFFFFF;
        
        replaceInt = replaceInt | newByte;
        
        intBuffer[theInt] = replaceInt;
        
        return intBuffer;
        
    }

    
    public byte[] getByteArray(int[] array)
    {
        byte[] newArray = new byte[array.length * 4];
        
        int pos = 0;
        for(int i = 0; i < array.length; i++)
        {
            if(littleEndian)
            {
                newArray[pos++] = (byte)((array[i] >> 0) & 0xFF);
                newArray[pos++] = (byte)((array[i] >> 8) & 0xFF);
                newArray[pos++] = (byte)((array[i] >> 16) & 0xFF);
                newArray[pos++] = (byte)((array[i] >> 24) & 0xFF);
            }
            else
            {
                newArray[pos++] = (byte)((array[i] >> 24) & 0xFF);
                newArray[pos++] = (byte)((array[i] >> 16) & 0xFF);
                newArray[pos++] = (byte)((array[i] >> 8) & 0xFF);
                newArray[pos++] = (byte)((array[i] >> 0) & 0xFF);
            }
        }
        
        return newArray;
    }
    
    public byte[] getByteArray(int integer)
    {
        int[] temp = new int[1];
        temp[0] = integer;
        return getByteArray(temp);
    }
}
