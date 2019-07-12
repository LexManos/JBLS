package BNLSProtocol;

public class BNLSlist {

  	private static final int CRC32_POLYNOMIAL = 0xEDB88320;
    private static long CRC32Table[] = new long[256];
	private static boolean CRC32Initialized = false;	
    public static String GetPassword(String sID) {
      return util.Ini.ReadIni("./bots.ini", sID, "Password", null);
    }
  private static void InitCRC32(){
		if(CRC32Initialized)
			return;
		CRC32Initialized = true;
		for(int I = 0; I < 256; I++) {
			long K = I;
			for(long J = 0; J < 8; J++){
				long XorVal = (((K & 1) > 0)  ? CRC32_POLYNOMIAL : 0);
				K = (K < 0 ? ((K & 0x7FFFFFFF) / 2) | 0x40000000 : K / 2);					
				K ^= XorVal;
			}
		    //K = (K >> 1) ^ (((K & 1) > 0)  ? CRC32_POLYNOMIAL : 0);
			CRC32Table[I] = K;
		}
	}

	private static long CRC32(char[] Data, long Size){
		InitCRC32();
		long CRC = 0xffffffff;
		int tableIndex = 0;
		for (int X = 0; X < Size; X++){
			tableIndex = (int)((CRC & 0xff) ^ Data[X]);
			
			if (CRC < 0)
				CRC = ((CRC & 0x7FFFFFFF) / 0x100) | 0x800000;
			else
				CRC /= 0x100;
				
			CRC ^= CRC32Table[tableIndex];
		    //CRC = (CRC >> 8)    ^ CRC32Table[(int)((CRC & 0xff) ^ Data[X])];
		}
		return ~CRC;
	}

	private static char Hex(char Digit){
	  return (Digit < 10 ? (char)(Digit + '0') : (char)(Digit - 10 + 'A'));
	}

	public static long BNLSChecksum(String Password, long ServerCode)
	{
		long Size = Password.length();
		char[] Data = new char[(int)(Size + 8)];
		for (int X = 0; X < Size; X++)
			Data[X] = Password.charAt(X);
		int I = 7;
		do {
			Data[(int)Size + I] = Hex((char)((char)ServerCode & 0xf));
			ServerCode >>= 4;
		}while((I-- > 0));
		long Checksum = CRC32(Data, Size + 8);
		return Checksum;
	}

  
}