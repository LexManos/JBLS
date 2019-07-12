/*
 * Created on Feb 5, 2005
 */
package util;

import java.math.BigInteger;
import java.util.Random;


public class BigIntegerEx
{
    public static final int BIG_ENDIAN = 0;
    public static final int LITTLE_ENDIAN = 1;
    public static final int BIGINT_SIZE = 32;
    
    private final BigInteger bigInteger;
    private final int endian;
    
    public BigIntegerEx(int endian, String value)
    {
        this.endian = endian;

        bigInteger = new BigInteger(value);
    }
    
    public BigIntegerEx(int endian, byte []value)
    {
        this.endian = endian;
        
        if(endian == BIG_ENDIAN)
            bigInteger = new BigInteger(1, value);
        else
            bigInteger = new BigInteger(1, reverseArray(value, value.length));
    }
    
    public BigIntegerEx(int endian, BigInteger base)
    {
        this.endian = endian;
        
        if(endian == LITTLE_ENDIAN)
            this.bigInteger = base;
        else
            this.bigInteger = new BigInteger(reverseArray(base.toByteArray(), BIGINT_SIZE));
    }
    
    public BigIntegerEx(int endian, int bits)
    {
        this.endian = endian;
        this.bigInteger = new BigInteger(bits, new Random());
    }
    
    public BigInteger getBigInteger()
    {
        return bigInteger;
    }
    
    public byte []toByteArray()
    {
        if(endian == BIG_ENDIAN)
            return bigInteger.toByteArray();
        
        return reverseArray(bigInteger.toByteArray(), BIGINT_SIZE);
    }
    
    public BigIntegerEx modPow(BigIntegerEx exponent, BigIntegerEx m)
    {
        return new BigIntegerEx(endian, getBigInteger().modPow(exponent.getBigInteger(), m.getBigInteger()));
    }
    
    public BigIntegerEx mod(BigIntegerEx m)
    {
        return new BigIntegerEx(endian, getBigInteger().mod(m.getBigInteger()));
    }
    
    public BigIntegerEx add(BigIntegerEx a)
    {
        return new BigIntegerEx(endian, getBigInteger().add(a.getBigInteger()));
    }
    
    public BigIntegerEx subtract(BigIntegerEx a)
    {
        return new BigIntegerEx(endian, getBigInteger().subtract(a.getBigInteger()));
    }
    
    public BigIntegerEx multiply(BigIntegerEx a)
    {
        return new BigIntegerEx(endian, getBigInteger().multiply(a.getBigInteger()));
    }
    
    public int compareTo(BigIntegerEx a)
    {
        return bigInteger.compareTo(a.getBigInteger());
    }
    
    public String toString()
    {
        return bigInteger.toString();
    }
    
    public String toString(int radix)
    {
        return bigInteger.toString(radix);
    }
    
    private static byte []reverseArray(byte []array, int maxLen)
    {
    	byte []a = new byte[array.length];
    	
    	for(int i = 0; i < array.length; i++)
    		a[array.length - i - 1] = array[i];
    	
    	if(maxLen != 0)
    		return trimArray(a, maxLen);
	    return a;
    }

    private static byte []trimArray(byte []array, int size)
    {
    	byte []a = new byte[size];
    	
    	if(array.length > size)
    		System.arraycopy(array, 0, a, 0, size);
    	else
    		System.arraycopy(array, 0, a, 0, array.length);
    	
    	return a;
    }
}
