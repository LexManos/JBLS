/*
 * IntFromByteArray.java
 *
 * Created on May 21, 2004, 12:35 PM
 */

package util;

/** This is a class to take care of inserting or getting the value of an int in an array of
 * bytes.
 */
public class IntFromByteArray
{
    private boolean littleEndian;
    
    public static final IntFromByteArray LITTLEENDIAN = new IntFromByteArray(true);
    public static final IntFromByteArray BIGENDIAN = new IntFromByteArray(false);
    
    /**
     * @param args the command line arguments
     */
    
    /*public static void main(String args[])
    {
        byte[] test = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        
        IntFromByteArray ifba = new IntFromByteArray(true);
        
        int[] newArray = ifba.getIntArray(test);

        
        for(int i = 0; i < newArray.length; i++)
            System.out.print(PadString.padHex(newArray[i], 8) + " ");
    }*/

    public IntFromByteArray(boolean littleEndian)
    {
        this.littleEndian = littleEndian;
    }
    public int getInteger(byte[] array, int location)
    {
        if((location + 3) >= array.length)
            throw new ArrayIndexOutOfBoundsException("location = " + location + ", number of bytes = " + array.length + " (note: 4 available bytes are needed)");
        
        int retVal = 0;
        
        // reverse the byte to simulate little endian
        if(littleEndian)
        {
            retVal = retVal | ((array[location++] << 0)  & 0x000000FF);
            retVal = retVal | ((array[location++] << 8)  & 0x0000FF00);
            retVal = retVal | ((array[location++] << 16) & 0x00FF0000);
            retVal = retVal | ((array[location++] << 24) & 0xFF000000);
        }
        else
        {
            retVal = retVal | ((array[location++] << 24) & 0xFF000000);
            retVal = retVal | ((array[location++] << 16) & 0x00FF0000);
            retVal = retVal | ((array[location++] << 8)  & 0x0000FF00);
            retVal = retVal | ((array[location++] << 0)  & 0x000000FF);
        }
        
        return retVal;
    }
    
    
    /** This function is used to insert the byte into a specified spot in
     * an int array.  This is used to simulate pointers used in C++.
     * Note that this works in little endian only.
     * @param intBuffer The buffer to insert the int into.
     * @param b The byte we're inserting.
     * @param location The location (which byte) we're inserting it into.
     * @return The new array - this is returned for convenience only.
     */
    public byte[] insertInteger(byte[] array, int location, int b)
    {
        if(location + 3 >= array.length)
            throw new ArrayIndexOutOfBoundsException("location = " + location + ", length = " + array.length + " - note that we need 4 bytes to insert an int");
        
        if(littleEndian)
        {
            array[location++] = (byte)((b & 0x000000FF) >> 0);
            array[location++] = (byte)((b & 0x0000FF00) >> 8);
            array[location++] = (byte)((b & 0x00FF0000) >> 16);
            array[location++] = (byte)((b & 0xFF000000) >> 24);
        }
        else
        {
            array[location++] = (byte)((b & 0xFF000000) >> 24);
            array[location++] = (byte)((b & 0x00FF0000) >> 16);
            array[location++] = (byte)((b & 0x0000FF00) >> 8);
            array[location++] = (byte)((b & 0x000000FF) >> 0);
        }
        
        return array;
    }
    
    /** Note: This will cut off the end bytes to ensure it's a multiple of 4 */
    public int[] getIntArray(byte[] array)
    {
        int[] newArray = new int[array.length / 4];
        
        int pos = 0;
        for(int i = 0; i < newArray.length; i++)
        {
            if(littleEndian)
            {
                newArray[i] |= ((array[pos++] << 0) &  0x000000FF);
                newArray[i] |= ((array[pos++] << 8) &  0x0000FF00);
                newArray[i] |= ((array[pos++] << 16) & 0x00FF0000);
                newArray[i] |= ((array[pos++] << 24) & 0xFF000000);
            }
            else
            {
                newArray[i] |= array[pos++] << 24;
                newArray[i] |= array[pos++] << 16;
                newArray[i] |= array[pos++] << 8;
                newArray[i] |= array[pos++] << 0;
            }
        }
        
        return newArray;
    }
    
}
