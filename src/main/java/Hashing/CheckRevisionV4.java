package Hashing;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import util.*;

/** 
 * This is the new CheckRevisionV4 introduced in 2019 for Diablo 2.
 * It comes in CheckRevision.mpq
 */
public class CheckRevisionV4 extends CheckRevisionV1
{
    /** Stores some past results */
	private static int Version[][] = new int[3][0x0C];
    private static Hashtable<String, CheckrevisionResults> crCache = new Hashtable<String, CheckrevisionResults>();
    private static int crCacheHits = 0;
    private static int crCacheMisses = 0;

    public static void clearCache(){
    	Version = new int[3][0x0C];
    	crCacheHits = 0;
    	crCacheMisses = 0;
    	crCache = new Hashtable<String, CheckrevisionResults>();
    	System.gc();
    }

    public static void main(String[] args) 
    {
    	CheckrevisionResults result = null;
    	String[] inputs = {"0RFf+AAA", "3ou3jQAA", "ZYDAHQAA", "Ry4VIgAA"};
    	for (int i = 0; i < inputs.length; i++) {
    		try {
        		System.out.println("in: " + inputs[i]);
    			result = checkRevision(inputs[i], Constants.PRODUCT_DIABLO2, Constants.PLATFORM_INTEL, "CheckRevision.mpq");
    			System.out.println("out: " + Integer.toHexString(result.getChecksum()) + " " + new String(result.getInfo().getBufferChar()));
    		} catch (IOException e) {
    			System.out.println("Test failed (IO error)");
    		}
    		System.out.println("");
    	}
    }
    
    public static CheckrevisionResults checkRevision(String value, int prod, byte plat, String mpq) throws FileNotFoundException, IOException
    {
        if(prod > 0x0B || plat > 3 || prod < 0 || plat < 0) return null;
        
        CheckrevisionResults cacheHit = (CheckrevisionResults)crCache.get(value + mpq + prod + plat);
        if(cacheHit != null){
            Out.println("CREV", "CheckRevision cache hit: " + crCacheHits + " hits, " + crCacheMisses + " misses.");
            crCacheHits++;
            return cacheHit;
        }
        crCacheMisses++;

        String version = Constants.IX86versions[prod-1];
        if (version.length() == 0) {
        	Out.error("CREV", "Simple check revision unavailable for " + Constants.prods[prod-1] + ": missing version number");
        	return null;
        }
        
        Version[plat][prod] = 0;
        
        Buffer seed = new Buffer(new Buffer(Base64.getDecoder().decode(value)).removeBytes(4));
        
        Buffer buff = new Buffer(seed.getBuffer());
        buff.addString(":" + version + ":");
        buff.addByte((byte)1);
        
        byte[] hash;
		try {
			hash = MessageDigest.getInstance("SHA-1").digest(buff.getBuffer());
		} catch (NoSuchAlgorithmException e) {
			Out.error("CREV", "Hashing failed - SHA1 not supported");
			return null;
		}
        String ret = Base64.getEncoder().encodeToString(hash);
        
        // Add the certificate hash if needed
        if (mpq.endsWith("D1.mpq")) {
        	Version[plat][prod] = 6;
        	
        	String certHex = Constants.IX86certs[prod-1];
        	if (certHex.length() == 0) {
        		Out.error("CREV", "Simple check revision unavailable for " + Constants.prods[prod-1] + ": missing certificate");
        		return null;
        	}
        	buff = new Buffer(hexToBytes(certHex));
        	buff.addBytes(seed.getBuffer());
        	
        	try {
        		hash = MessageDigest.getInstance("SHA-1").digest(buff.getBuffer());
        	} catch (NoSuchAlgorithmException e) {
        		Out.error("CREV", "Hashing failed - SHA1 not supported (step 2)");
        		return null;
        	}
        	
        	ret += ":" + Base64.getEncoder().encodeToString(hash);
        }
        
        byte[] checkB = ret.substring(0, 4).getBytes();
        
        // Flip the byte order
        for (int i = 0, j = checkB.length - 1; i < j; i++, j--) {
        	byte temp = checkB[i];
        	checkB[i] = checkB[j];
        	checkB[j] = temp;
        }
        int checksum = new BigInteger(checkB).intValue();
        if (checksum == 0) return null;
        
        Buffer info = new Buffer(ret.substring(4).getBytes());
        info.addByte((byte)0);	// Null terminator

        CheckrevisionResults result = new CheckrevisionResults(Version[plat][prod], checksum, info);
        crCache.put(value + mpq + prod + plat, result);
        return result;
    }
    
    public static byte[] hexToBytes(String hex) {
    	byte[] value = new byte[hex.length() / 2];
    	for (int i = 0; i < value.length; i++) {
    		int index = i * 2;
    		value[i] = (byte)Integer.parseInt(hex.substring(index, index + 2), 16);
    	}
    	return value;
    }
}