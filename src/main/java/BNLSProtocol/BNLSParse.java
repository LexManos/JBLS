/*
 * Created on Sep 24, 2004
 *
 * This class parses all the data from BNLS Input
 */
/*
 * This Class Does all the parsing of the BNLS Packets.  Deciding what to
 * take out, what to return, etc.  Calls on HashMain for hashing Functions.
 *
 * Also stores individual connection specific variables
 */

package BNLSProtocol;

import java.util.Hashtable;
import java.util.Random;

import util.Buffer;
import util.Constants;
import util.Controller;
import util.Out;
import util.PadString;
import Hashing.BrokenSHA1;
import Hashing.DoubleHash;
import Hashing.HashException;
import Hashing.HashMain;
import Hashing.SRP;
import Hashing.CheckrevisionResults;

public class BNLSParse{
  // The Following are Variables that are unique to this specific connection
  /** Whether or not the client is authorized to request hash info */
  private boolean authorized = (!Constants.requireAuthorization);
  private String BNLSUsername, BNLSPassword;// Username/Password of the current client
  private BNLSConnectionThread connection = null;

  /** used to check the status of the BNLS Account/Password */
  private int BNLSServerCode;
  private int nlsRevision = 1;// NLS Revision number that wants to be used
  private SRP mySRP;          // Stored SRP...Needed for when we go from 0x02 to 0x03
  private SRP myNewSRP;       // For password changes

  /** salt and B are stored for the server's proof */
  private byte []salt = null;
  private byte []B = null;
  private String oldPass = null;
  private SRP []reservedSRPs = null;
  private int SRPs = 0;

  private static final byte BNLS_NULL                  = 0x00; //Fully Supported
  private static final byte BNLS_CDKEY                 = 0x01; //Fully Supported
  private static final byte BNLS_LOGONCHALLENGE        = 0x02; //Fully Supported
  private static final byte BNLS_LOGONPROOF            = 0x03; //Fully Supported
  private static final byte BNLS_CREATEACCOUNT         = 0x04; //Fully Supported
  private static final byte BNLS_CHANGECHALLENGE       = 0x05; //Fully Supported
  private static final byte BNLS_CHANGEPROOF           = 0x06; //Fully Supported
  private static final byte BNLS_UPGRADECHALLENGE      = 0x07; //Fully Supported
  private static final byte BNLS_UPGRADEPROOF          = 0x08; //fully Supported
  private static final byte BNLS_VERSIONCHECK          = 0x09; //Fully Supported
  private static final byte BNLS_CONFIRMLOGON          = 0x0a; //Fully Supported
  private static final byte BNLS_HASHDATA              = 0x0b; //Fully Supported
  private static final byte BNLS_CDKEY_EX              = 0x0c; //Fully Supported
  private static final byte BNLS_CHOOSENLSREVISION     = 0x0d; //Fully Supported
  private static final byte BNLS_AUTHORIZE             = 0x0e; //Fully Supported
  private static final byte BNLS_AUTHORIZEPROOF        = 0x0f; //Fully Supported
  private static final byte BNLS_REQUESTVERSIONBYTE    = 0x10; //Fully Supported
  private static final byte BNLS_VERIFYSERVER          = 0x11; //Fully Supported
  private static final byte BNLS_RESERVESERVERSLOTS    = 0x12; //Fully Supported
  private static final byte BNLS_SERVERLOGONCHALLENGE  = 0x13; //Fully Supported
  private static final byte BNLS_SERVERLOGONPROOF      = 0x14; //Fully Supported
  private static final byte BNLS_RESERVED0             = 0x15;
  private static final byte BNLS_RESERVED1             = 0x16;
  private static final byte BNLS_RESERVED2             = 0x17;
  private static final byte BNLS_VERSIONCHECKEX        = 0x18; //Fully Supported
  private static final byte BNLS_RESERVED3             = 0x19;
  private static final byte BNLS_VERSIONCHECKEX2       = 0x1A; //Fully Supported
  
  /*The IDs of each client, Used in versioncheck*/
  /* private static final byte PRODUCT_STARCRAFT          = 0x01; //Fully supported
   * private static final byte PRODUCT_BROODWAR           = 0x02; //Fully Supported
   * private static final byte PRODUCT_WAR2BNE            = 0x03; //Fully Supported
   * private static final byte PRODUCT_DIABLO2            = 0x04; //Fully Supported
   * private static final byte PRODUCT_LORDOFDESTRUCTION  = 0x05; //Fully Supported
   * private static final byte PRODUCT_JAPANSTARCRAFT     = 0x06; //Fully Supported
   * private static final byte PRODUCT_WARCRAFT3          = 0x07; //Fully Supported
   * private static final byte PRODUCT_THEFROZENTHRONE    = 0x08; //Fully Supported
   * private static final byte PRODUCT_DIABLO             = 0x09; //Fully Supported
   * private static final byte PRODUCT_DIABLOSHAREWARE    = 0x0A; //Fully Supported
   * private static final byte PRODUCT_STARCRAFTSHAREWARE = 0x0B; //Fully Supported
   */
  
  /*Flag definitions for BNLS_CDKEY_EX*/
  private static final byte CDKEY_SAME_SESSION_KEY          = 0x01;//Fully Supported
  private static final byte CDKEY_GIVEN_SESSION_KEY         = 0x02;//Fully Supported
  private static final byte CDKEY_MULTI_SERVER_SESSION_KEYS = 0x04;//Fully Supported
  private static final byte CDKEY_OLD_STYLE_RESPONSES       = 0x08;//Fully Supported
  /*Flag definitions for BNLS_HASHDATA*/
  private static final byte HASHDATA_FLAG_DOUBLEHASH = 0x02;//Fully Supported
  private static final byte HASHDATA_FLAG_COOKIE     = 0x04;//Fully Supported
  
  public static Hashtable<String, Integer> botIds;

  public BNLSParse(BNLSConnectionThread conn){ this.connection = conn; }
  
  public OutPacketBuffer parseInput(byte packetID, short pLength, String data) throws InvalidPacketException, BNLSException{
    return parseInput(new InPacketBuffer(packetID, pLength, data));
  }

  /* @param in - PacketBuffer to be Parsed
   * @throws InvalidPacketException - when packet is malformed/incorrect
   * @throws BNLSException - when a connection-fatal BNLS problem has occured
   * @return OutPacketBuffer - Buffer representing a packet to be sent in response
   */
  public OutPacketBuffer parseInput(InPacketBuffer in) throws InvalidPacketException, BNLSException{
    int packetID = in.getPacketID();
    /* Check For Authorization. If not authorized, they can only send
      * 0x0E(BNLS Account) or 0x0F(BNLS Pass Authorization)
      */
    if (!authorized && (packetID != 0x00 && packetID != 0x0e && packetID != 0x0F))
      throw new BNLSException("Attempted to Access Info When Not Authorized");
    try{
      switch (packetID){
        case BNLS_NULL:                 return onNull(in);
        case BNLS_CDKEY:                return onCDKey(in);
        case BNLS_LOGONCHALLENGE:       return onLogonChallenge(in);
        case BNLS_LOGONPROOF:           return onLogonProof(in);
        case BNLS_CREATEACCOUNT:        return onCreateAccount(in);
        case BNLS_CHANGECHALLENGE:      return onChangeChallenge(in);
        case BNLS_CHANGEPROOF:          return onChangeProof(in);
        case BNLS_UPGRADECHALLENGE:     return onUpgradeChallenge(in);
        case BNLS_UPGRADEPROOF:         return onUpgradeProof(in);
        case BNLS_VERSIONCHECK:         return onVersionCheck(in);
        case BNLS_CONFIRMLOGON:         return onConfirmLogon(in);
        case BNLS_HASHDATA:             return onHashData(in);
        case BNLS_CDKEY_EX:             return onCDKeyEx(in);
        case BNLS_CHOOSENLSREVISION:    return onChooseNLSRevision(in);
        case BNLS_AUTHORIZE:            return onAuthorize(in);
        case BNLS_AUTHORIZEPROOF:       return onAuthorizeProof(in);
        case BNLS_REQUESTVERSIONBYTE:   return onRequestVersionByte(in);
        case BNLS_VERIFYSERVER:         return onVerifyServer(in);
        case BNLS_RESERVESERVERSLOTS:   return onReserveServerSlots(in);
        case BNLS_SERVERLOGONCHALLENGE: return onServerLogonChallenge(in);
        case BNLS_SERVERLOGONPROOF:     return onServerLogonProof(in);
        case BNLS_RESERVED0:            return onUnknown(in);
        case BNLS_RESERVED1:            return onUnknown(in);
        case BNLS_RESERVED2:            return onUnknown(in);
        case BNLS_VERSIONCHECKEX:       return onVersionCheckEX(in);
        case BNLS_RESERVED3:            return onUnknown(in);
        case BNLS_VERSIONCHECKEX2:      return onVersionCheckEX2(in);
        default:                        return onUnknown(in);
      }
    }catch (IndexOutOfBoundsException e){
      Out.error("JBLS Parse", "Index out of bounds Exception");
      e.printStackTrace();
      throw new InvalidPacketException("Packet not Long Enough(Array out of Bounds)" + e.toString());
    }
  }
  /* This jsut compares two bytes arrays and make sure they are equal*/
  private boolean equal(byte[] a, byte[] b){
    if (a.length != b.length) return false;
    for (int I = 0; I < a.length; I++)
      if (a[I] != b[I]) return false;
    return true;
  }
    
  private OutPacketBuffer onNull(InPacketBuffer in){
    /* BNLS_NULL (0x00)
      * ----------------
      * This message is empty and may be used to keep the
      * connection alive. The client is not required to
      * send this. There is no response from the server.
      */
    return null;
  }
  private OutPacketBuffer onCDKey(InPacketBuffer in){
    /* BNLS_CDKEY (0x01)
      * -----------------
      * This message will encrypt your CD-key,
      * and will reply with the properly encoded CD-key
      * as it is supposed to be sent in the message
      * SID_AUTH_CHECK (0x51).
      * It now works with CD-keys of all products.
      * (DWORD) Session key from Battle.net.
      *     This is the second DWORD in SID_AUTH_INFO (0x50).
      * (String) CD-key.
      *    No dashes or spaces.
      * Response:
      * ---------
      * (BOOL) Success (TRUE if successful, FALSE otherwise).
      *    If this is FALSE, there is no more data in this message.
      * (DWORD) Client session key.
      * (9 DWORDs) CD-key data.
      */
    int client = Math.abs(new Random().nextInt());
    int server = in.removeDWord();
    Buffer hash = new Buffer();
    String key = in.removeNTString();
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_CDKEY);
    try{
      hash = HashMain.hashKey(client, server, key);
    }catch (HashException e){
      Out.error("JBLS Parse", "0x01 Key Hash Failed");
      reply.addDWord(0);
      return reply;
    }

    reply.addDWord(1);
    reply.addDWord(client);
    reply.addBuffer(hash);
    return reply;
  }
  private OutPacketBuffer onLogonChallenge(InPacketBuffer in){
    /* BNLS_LOGONCHALLENGE (0x02)
      * --------------------------
      * This message will give you data you need for
      * SID_AUTH_ACCOUNTLOGON (0x53).
      * You must send this before you can send BNLS_LOGONPROOF (0x03).
      * (String) Account name.
      * (String) Account password.
      * Response:
      * ---------
      * (8 DWORDs) Data for SID_AUTH_ACCOUNTLOGON (0x53).
      *    -(DWORD[8]) A
      */
    String name = in.removeNTString();
    String pass = in.removeNTString();
    if (name == null || pass == null) return null;
    mySRP = new SRP(name, pass);
    mySRP.set_NLS(nlsRevision);
    if (Constants.displayParseInfo)
      Out.info("JBLS Parse", ">>> \"" + name + "\" WAR3 Account Login ");
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_LOGONCHALLENGE);
    reply.add(mySRP.get_A());
    return reply;
  }
  private OutPacketBuffer onLogonProof(InPacketBuffer in) throws BNLSException{
    /* BNLS_LOGONPROOF (0x03)
      * ----------------------
      * This message will parse data from SID_AUTH_ACCOUNTLOGON (0x53)
      * and will reply with data to send in SID_AUTH_ACCOUNTLOGONPROOF (0x54).
      * You must send BNLS_LOGONCHALLENGE (0x02) before you can send this.
      * This message cannot be used simultaneously with
      * BNLS_CHANGEPROOF (0x06) or BNLS_UPGRADEPROOF (0x08).
      * (16 DWORDs) Data from SID_AUTH_ACCOUNTLOGON (0x53).
      *     -(DWORD[8]) salt
      *     -(DWORD[8]) B
      * Response:
      * ---------
      * (5 DWORDs) Data for SID_AUTH_ACCOUNTLOGONPROOF (0x54).
      *     -(DWORD[5]) M1
      */
    if (mySRP == null)
      throw new BNLSException("Must send BNLS_LOGONCHALLENGE Before BNLS_LOGONPROOF");
    salt = in.removeBytes(SRP.BIGINT_SIZE);
    B = in.removeBytes(SRP.BIGINT_SIZE);
    
    mySRP.set_NLS(nlsRevision);
	
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_LOGONPROOF);
    reply.add(mySRP.getM1(salt, B));
    return reply;
  }
  private OutPacketBuffer onCreateAccount(InPacketBuffer in){
    /* BNLS_CREATEACCOUNT (0x04)
      * -------------------------
      * This message will give you data you need for
      * SID_AUTH_ACCOUNTCREATE (0x52).
      * (String) Account name.
      * (String) Account password.
      * Response:
      * ---------
      * (16 DWORDs) Data for SID_AUTH_ACCOUNTCREATE (0x52).
      *    -(DWORD[8]) salt
      *    -(DWORD[8]) v
      */
    String name = in.removeNTString();
    String pass = in.removeNTString();
    mySRP = new SRP(name, pass);
    mySRP.set_NLS(nlsRevision);
    // We need a random variable for salt, and guess what A is?
    salt = mySRP.get_A();
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_CREATEACCOUNT);
    reply.add(salt);
    reply.add(mySRP.get_v(salt).toByteArray());
    return reply;
  }
  private OutPacketBuffer onChangeChallenge(InPacketBuffer in){
    /* BNLS_CHANGECHALLENGE (0x05)
      * ---------------------------
      * This message will give you data you need for
      * SID_AUTH_ACCOUNTCHANGE (0x55).
      * This message is used to change the password
      * of an existing account. You must send
      * this before you can send BNLS_CHANGEPROOF (0x06).
      * (String) Account name.
      * (String) Account old password.
      * (String) Account new password.
      * Response:
      * ---------
      * (8 DWORDs) Data for SID_AUTH_ACCOUNTCHANGE (0x55).
      *    -(DWORD[8]) A
      */
    String name = in.removeNTString();
    String oldPass = in.removeNTString();
    String accPass = in.removeNTString();
    mySRP = new SRP(name, oldPass);
    myNewSRP = new SRP(name, accPass);
    mySRP.set_NLS(nlsRevision);
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_CHANGECHALLENGE);
    reply.add(mySRP.get_A());
    return reply;
  }
  private OutPacketBuffer onChangeProof(InPacketBuffer in){
    /* BNLS_CHANGEPROOF (0x06)
      * -----------------------
      * This message will parse data from SID_AUTH_ACCOUNTCHANGE
      * (0x55) and will reply with data to send in
      * SID_AUTH_ACCOUNTCHANGEPROOF (0x56).
      * You must send BNLS_CHANGECHALLENGE (0x05) before you can send this.
      * This message cannot be used simultaneously with
      * BNLS_LOGONPROOF (0x03) or BNLS_UPGRADEPROOF (0x08).
      *
      * (16 DWORDs) Data from SID_AUTH_ACCOUNTCHANGE (0x55).
      *   -(DWORD[8]) salt
      *   -(DWORD[8]) B
      * Response:
      * ---------
      * (21 DWORDs) Data for SID_AUTH_ACCOUNTCHANGEPROOF (0x56).
      *    -(DWORD[8]) M1
      *    -(DWORD[8]) Salt
      *    -(DWORD[8]) v	  
      */
    if(mySRP == null) return null;
    if(myNewSRP == null) return null;
    salt = in.removeBytes(SRP.BIGINT_SIZE);
    B = in.removeBytes(SRP.BIGINT_SIZE);
    mySRP.set_NLS(nlsRevision);
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_CHANGEPROOF);
    reply.add(mySRP.getM1(salt, B));
    reply.add(salt);
    reply.add(myNewSRP.get_v(salt).toByteArray());
    return reply;
  }
  private OutPacketBuffer onUpgradeChallenge(InPacketBuffer in){
    /* BNLS_UPGRADECHALLENGE (0x07)
      * ----------------------------
      * This message will give you data you need for
      * SID_AUTH_ACCOUNTUPGRADE (0x57). This message is used to
      * upgrade an existing account from Old Logon System to New
      * Logon System.
      * You must send this before you can send BNLS_UPGRADEPROOF (0x08).
      * Important:
      *    You must send BNLS_LOGONCHALLENGE (0x02) or BNLS_CHANGECHALLENGE (0x05)
      *    before sending this. Otherwise, the results are meaningless.
      * Note: Since Old Logon System and New Logon
      * System are incompatible, you can change the password and
      * upgrade the account at the same time. This is not
      * required - the old password and the new password may be
      * identical for this message.
      * (String) Account name.
      * (String) Account old password.
      * (String) Account new password.
      *     (May be identical to old password but still must be provided.)
      * Response:
      * ---------
      * (BOOL) Success code.
      *     If this is TRUE, you may send SID_AUTH_ACCOUNTUPGRADE (0x57).
      * Currently, no error conditions are defined, so this is always TRUE.
      */
     String name = in.removeNTString();
     oldPass = in.removeNTString();
     mySRP = new SRP(name, in.removeNTString());
     mySRP.set_NLS(nlsRevision);

    OutPacketBuffer reply = new OutPacketBuffer(BNLS_UPGRADECHALLENGE);
    reply.addDWord(true);
    return reply;
  }
  private OutPacketBuffer onUpgradeProof(InPacketBuffer in){
    /* BNLS_UPGRADEPROOF (0x08)
      * ------------------------
      * This message will parse data from SID_AUTH_ACCOUNTUPGRADE
      * (0x57) and will reply with data to send in
      * SID_AUTH_ACCOUNTUPGRADEPROOF (0x58).
      * You must send BNLS_UPGRADECHALLENGE (0x07) before you can
      * send this.
      * This message cannot be used simultaneously with
      * BNLS_LOGONPROOF (0x03) or BNLS_CHANGEPROOF (0x06).
      *
      * (DWORD) Session key from SID_AUTH_ACCOUNTUPGRADE (0x57).
      * Response:
      * ---------
      * (22 DWORDs) Data for SID_AUTH_ACCOUNTUPGRADEPROOF (0x58).
      *
      * The 22 DWORD responsein comprised of the following:
      *  (DWORD) Client Token
      *  (DWORD[5]) Double XSHA-1 password hash (Old password)
      *  (DWORD[8]) Salt
      *  (DWORD[8]) Password verifyer
      */
    if (mySRP == null) return null;
    int server = in.removeDWord();
    int client = Math.abs(new Random().nextInt());
    int[] xsha = DoubleHash.doubleHash(oldPass, server, client);
    mySRP.set_NLS(nlsRevision);
    salt = mySRP.get_A();
	
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_UPGRADEPROOF);
    reply.addDWord(client);
    for (int x = 0; x < 5; x++)
      reply.addDWord(xsha[x]);
    reply.add(salt);
    reply.add(mySRP.get_v(salt).toByteArray());
    return reply;
  }
  private OutPacketBuffer onVersionCheck(InPacketBuffer in){
    /* BNLS_VERSIONCHECK (0x09)
      *------------------------
      * This message will request a fast version check. Now works with all products.
      * (DWORD) Product ID.
      * (DWORD) Version DLL digit in the range 0-7. (For example, for IX86Ver1.mpq this is 1)
      * (String) Checksum formula.
      * Response:
      * ---------
      * (BOOL) Success (TRUE if successful, FALSE otherwise). If this is FALSE, there is no more data in this message.
      * (DWORD) Version.
      * (DWORD) Checksum.
      * (String) Version check stat string.
      */
    int prod = in.removeDWord();
    int archiveDig = in.removeDWord();
    String formula = in.removeNTString();
    String archive = "ver-IX86-" + archiveDig + ".mpq";
    if(prod == Constants.PRODUCT_STARCRAFT       ||
       prod == Constants.PRODUCT_BROODWAR        ||
       prod == Constants.PRODUCT_WAR2BNE         ||
       prod == Constants.PRODUCT_JAPANSTARCRAFT  ||
       prod == Constants.PRODUCT_DIABLO          ||
       prod == Constants.PRODUCT_DIABLOSHAREWARE ||
       prod == Constants.PRODUCT_STARCRAFTSHAREWARE)
       archive = "lockdown-IX86-" + PadString.padNumber(archiveDig, 2) + ".mpq";
    
    CheckrevisionResults revision = HashMain.getRevision(prod, formula, archive, (long)0);
    int versionByte = HashMain.getVerByte(prod);
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_VERSIONCHECK);
    if(revision == null || versionByte == 0){
      reply.addDWord(0);
      return reply;
    }
    reply.addDWord(1);
    reply.addDWord(revision.getVersion());
    reply.addDWord(revision.getChecksum());
    reply.addBuffer(revision.getInfo());
    if(Controller.stats != null) Controller.stats.onCheckRevision(this.BNLSUsername, this.connection.IP, archive, prod);
    return reply;
  }   
  private OutPacketBuffer onConfirmLogon(InPacketBuffer in) throws BNLSException{
    /* BNLS_CONFIRMLOGON (0x0a)
      * ------------------------
      * This message will confirm that the server really knows your
      * password. May be used after "proof" messages:
      * BNLS_LOGONPROOF (0x03), BNLS_CHANGEPROOF (0x06),
      * BNLS_UPGRADEPROOF (0x08).
      * (5 DWORDs) Password proof from Battle.net.
      * Response:
      * ---------
      * (BOOL) TRUE if the server knows your password, FALSE otherwise.
      *   If this is FALSE, the Battle.net connection should be closed by the client.
      */
     if(salt == null || B == null)
       throw new BNLSException("Error: BNLS_LOGONPROOF has to be sent before BNLS_CONFIRMLOGON");
     mySRP.set_NLS(nlsRevision);
     byte []givenProof = in.removeBytes(SRP.SHA_DIGESTSIZE);
     byte []realProof = mySRP.getM2(salt, B);
     OutPacketBuffer reply = new OutPacketBuffer(BNLS_CONFIRMLOGON);
     reply.add(equal(givenProof, realProof) ? 1 : 0);
    return reply;
  }
  private OutPacketBuffer onHashData(InPacketBuffer in){
    /* BNLS_HASHDATA (0x0b)
      * --------------------
      * This message will calculate the hash of the given data.
      * The hashing algorithm used is the Battle.net standard
      * hashing algorithm also known as "broken SHA-1".
      * (DWORD) The size of the data to be hashed.
      *         Note: This is no longer restricted to 64 bytes.
      * (DWORD) Flags.
      * (VOID)  Data to be hashed.
      * (Optional DWORD) Client key.
      *      Present only if HASHDATA_FLAG_DOUBLEHASH (0x02) is specified.
      * (Optional DWORD) Server key.
      *      Present only if HASHDATA_FLAG_DOUBLEHASH (0x02) is specified.
      * (Optional DWORD) Cookie.
      *      Present only if HASHDATA_FLAG_COOKIE (0x04) is specified.
      *
      * The flags may be zero, or any bitwise combination of the defined
      * flags. Currently, the following flags are defined:
      *    #define HASHDATA_FLAG_UNUSED (0x01)
      *    #define HASHDATA_FLAG_DOUBLEHASH (0x02)
      *    #define HASHDATA_FLAG_COOKIE (0x04)
      *
      * HASHDATA_FLAG_UNUSED (0x01):
      *    This flag has no effect.
      * HASHDATA_FLAG_DOUBLEHASH (0x02):
      *    If this flag is present, the server will calculate a
      *    double hash. First it will calculate the hash of the
      *    data. Then it will prepend the client key and the server
      *    key to the resulting hash, and calculate the hash of the
      *    result. If this flag is present, the client key and
      *    server key DWORDs must be specified in the request after
      *    the data. This may be used to calculate password hashes
      *    for the "Old Logon System".
      * HASHDATA_FLAG_COOKIE (0x04):
      *    If this flag is present, a cookie DWORD is specified in
      *    the request. This is an application-defined value that is
      *    echoed back to the client in the response.
      * Response:
      * ---------
      * (5 DWORDs) The data hash.
      * (Optional DWORD) Cookie.
      *   Same as the cookie from the request. Present only
      *   if HASHDATA_FLAG_COOKIE (0x04) is specified.
      */
    int length = in.removeDWord();
    int flags = in.removeDWord();
    int[] hash;
    Buffer data = new Buffer();
    for(int x = 0; x < length; x++)
      data.add(in.removeByte());
    if ((flags & HASHDATA_FLAG_DOUBLEHASH) == HASHDATA_FLAG_DOUBLEHASH){
      int cToken = in.removeDWord();
      int sToken = in.removeDWord();
      hash = DoubleHash.doubleHash(new String(data.getBuffer()), cToken, sToken);
    }else
      hash = BrokenSHA1.calcHashBuffer(data.getBuffer());
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_HASHDATA);
    for (int x = 0; x < 5; x++)
      reply.add(hash[x]);

    if ((flags & HASHDATA_FLAG_COOKIE) == HASHDATA_FLAG_COOKIE)
      reply.add(in.removeDWord());
    return reply;
  }
  private OutPacketBuffer onCDKeyEx(InPacketBuffer in) throws InvalidPacketException{
    /* BNLS_CDKEY_EX (0x0c)
      * --------------------
      * This message will encrypt your CD-key or CD-keys using
      * the given flags.
      * (DWORD) Cookie.
      * (BYTE) Amount of CD-keys to encrypt. Must be between 1 and 32.
      * (DWORD) Flags.
      * (DWORD or DWORDs) Server session key(s), depending on the flags.
      * (Optional DWORD or DWORDs) Client session key(s), depending on the flags.
      * (String or strings) CD-keys. No dashes or spaces.
      *
      * The client can use multiple types of CD-keys in the same
      * packet. The flags may be zero, or any bitwise combination
      * of the defined flags.
      * Currently, the following flags are defined:
      * #define CDKEY_SAME_SESSION_KEY (0x01)
      * #define CDKEY_GIVEN_SESSION_KEY (0x02)
      * #define CDKEY_MULTI_SERVER_SESSION_KEYS (0x04)
      * #define CDKEY_OLD_STYLE_RESPONSES (0x08)
      *
      * CDKEY_SAME_SESSION_KEY (0x01):
      *    This flag specifies that all the returned CD-keys
      *    will use the same client session key. When used in
      *    combination with CDKEY_GIVEN_SESSION_KEY (0x02), a
      *    single client session key is specified immediately
      *    after the server session key(s). When used without
      *    CDKEY_GIVEN_SESSION_KEY (0x02), a client session key
      *    isn't sent in the request, and the server will create
      *    one. When not used, each CD-key gets its own client
      *    session key. This flag has no effect if the amount of
      *    CD-keys to encrypt is 1.
      * CDKEY_GIVEN_SESSION_KEY (0x02):
      *    This flag specifies that the client session keys to be
      *    used are specified in the request. When used in
      *    combination with CDKEY_SAME_SESSION_KEY (0x01), a single
      *    client session key is specified immediately after the
      *    server session key(s). When used without
      *    CDKEY_SAME_SESSION_KEY (0x01), an array of client session
      *    keys (as many as the amount of CD-keys) is specified.
      *    When not used, client session keys aren't included in the
      *    request.
      * CDKEY_MULTI_SERVER_SESSION_KEYS (0x04):
      *    This flag specifies that each CD-key has its own server
      *    session key. When specified, an array of server session
      *    keys (as many as the amount of CD-keys) is specified.
      *    When not specified, a single server session key is
      *    specified. This flag has no effect if the amount of
      *    CD-keys to encrypt is 1.
      * CDKEY_OLD_STYLE_RESPONSES (0x08):
      *    Specifies that the response to this packet is a
      *    number of BNLS_CDKEY (0x01) responses, instead of a
      *    BNLS_CDKEY_EX (0x0c) response. The responses are
      *    guaranteed to be in the order of the CD-keys' appearance
      *    in the request. Note that when this flag is specified,
      *    the Cookie cannot be echoed. (It must still be included
      *    in the request.)
      * Note:
      *    When using Lord of Destruction, two CD-keys are encrypted,
      *    and they must share the same client session key. There are
      *    several ways to do this:
      *    One way is to provide both CD-keys in BNLS_CDKEY_EX
      *    (0x0c) using the flag CDKEY_SAME_SESSION_KEY (0x01).
      *    Another way is to use BNLS_CDKEY (0x01) to encrypt the
      *    first CD-key, then use BNLS_CDKEY_EX (0x0c) using the
      *    flag CDKEY_GIVEN_SESSION_KEY (0x02) to encrypt the second
      *    CD-key with the same client session key.
      * Response:
      * ---------
      * When the flags don't contain CDKEY_OLD_STYLE_RESPONSES (0x08),
      * the response is a BNLS_CDKEY_EX (0x0c) message:
      * (DWORD) Cookie. Same as the value sent to the server in the request.
      * (BYTE) Amount of CD-keys that were requested.
      * (BYTE) Amount of CD-keys that were successfully encrypted.
      * (DWORD) Bit mask for the success code of each CD-key.
      *         Each bit of the 32 bits in this DWORD is 1 for success
      *         or 0 for failure. The least significant bit specifies
      *         the success code of the first CD-key provided. Bits
      *         that exceed the amount of CD-keys provided are set to 0.
      * The following fields repeat for each successful
      * CD-key (they do not exist for failed CD-keys):
      * (DWORD) Client session key.
      * (9 DWORDs) CD-key data.
      */
    int cookie = in.removeDWord();
    byte numKeys = in.removeByte();
    int[] serverKeys, clientKeys;
    boolean sameServerKey, sameClientKey;
    if (numKeys <= 0) throw new InvalidPacketException("No keys specified in BNLS_CDKEY_EX");
    int flags = in.removeDWord();
	
	//Grab out the Server Tokens
    if ((flags & CDKEY_MULTI_SERVER_SESSION_KEYS) == CDKEY_MULTI_SERVER_SESSION_KEYS){
      serverKeys = new int[numKeys];
      for (int x = 0; x < numKeys; x++)
        serverKeys[x] = in.removeDWord();
      sameServerKey = false;
    }else{
      serverKeys = new int[1];
      serverKeys[0] = in.removeDWord();
      sameServerKey = true;
    }
	//Grab out the Client tokens
    if ((flags & CDKEY_SAME_SESSION_KEY) == CDKEY_SAME_SESSION_KEY){
      clientKeys = new int[1];
      if ((flags & CDKEY_GIVEN_SESSION_KEY) == CDKEY_GIVEN_SESSION_KEY)
	    clientKeys[0] = in.removeDWord();
      else
        clientKeys[0] = Math.abs(new Random().nextInt());
      sameClientKey = true;
    }else{
      clientKeys = new int[numKeys];
      if ((flags & CDKEY_GIVEN_SESSION_KEY) == CDKEY_GIVEN_SESSION_KEY)
        for (int x = 0; x < numKeys; x++) clientKeys[x] = in.removeDWord();
      else{
        Random r = new Random();
        for (int x = 0; x < numKeys; x++) clientKeys[x] = Math.abs(r.nextInt());
      }
      sameClientKey = false;
    }
	
    Buffer[] hashedKey = new Buffer[numKeys];
    int cClientToken = clientKeys[0];
    int cServerToken = serverKeys[0];
    byte successKeys = numKeys;
    int successBitMask = 0;
    for (int x = 0; x < numKeys; x++){
      String keyToHash = in.removeNTString();
      if (!sameClientKey) cClientToken = clientKeys[x];
      if (!sameServerKey) cServerToken = serverKeys[x];
      try{
        hashedKey[x] = HashMain.hashKey(cClientToken, cServerToken, keyToHash);
        successBitMask = (successBitMask | (int) Math.pow(2.0, x));
      }catch (HashException e){
        //System.out.println("[0x0C] Invalid Key: " + keyToHash);
        successKeys--;
      }
    }

    if((flags & CDKEY_OLD_STYLE_RESPONSES) == CDKEY_OLD_STYLE_RESPONSES){
	  for(int x = 0; x < numKeys; x++){
	    OutPacketBuffer oldstyle = new OutPacketBuffer(BNLS_CDKEY);
		int success = (successBitMask & (int)Math.pow(2.0, x));
		if(success == 0){
		  oldstyle.addDWord(0);
		  this.connection.send(oldstyle);
		}else{
		  oldstyle.addDWord(1);
	      oldstyle.addDWord((sameClientKey ? clientKeys[0] : clientKeys[x]));
          oldstyle.addBuffer(hashedKey[x]);
		  this.connection.send(oldstyle);
		}
	  }
	  return null;
	}else{
      OutPacketBuffer reply = new OutPacketBuffer(BNLS_CDKEY_EX);
      reply.addDWord(cookie);
      reply.addByte(numKeys);
      reply.addByte(successKeys);
      reply.addDWord(successBitMask);
      for (int x = 0; x < numKeys; x++){
        if (hashedKey[x] != null){
	      reply.addDWord((sameClientKey ? clientKeys[0] : clientKeys[x]));
          reply.addBuffer(hashedKey[x]);
        }
      }
      return reply;
	}
  }
  private OutPacketBuffer onChooseNLSRevision(InPacketBuffer in){
    /* BNLS_CHOOSENLSREVISION (0x0d)
      * -------------------------------
      * This message instructs the server which revision of NLS you want to use.
      * (DWORD) NLS revision number.
      *         The NLS revision number is given by Battle.net in SID_AUTH_INFO (0x50).
      * Response:
      * ---------
      * (BOOL) Success code.
      *        If this is TRUE, the revision number was recognized
      *        by the server and will be used. If this is
      *        FALSE, the revision number was rejected by the server
      *        and this request is ignored.
      * NOTE: The default revision number is 1. Therefore, if Battle.net
      * reports a revision number of 1, this message may be omitted.
      */
    nlsRevision = in.removeDWord();
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_CHOOSENLSREVISION);
    if (nlsRevision < 0 || nlsRevision > 2){
      reply.addDWord(0);
      nlsRevision = 1;
    }else
      reply.addDWord(1);
    return reply;
  }
  private OutPacketBuffer onAuthorize(InPacketBuffer in){
    /* BNLS_AUTHORIZE (0x0e)
      * ---------------------
      *
      * NOTE: You no longer have to send this. This message logs
      * on to the BNLS server.
      *
      * (String) Bot ID.
      *
      * Note: The bot ID is not case sensitive, and is limited to
      * 31 characters. This message must be sent before sending
      * any other message.
      * Response:
      * ---------
      * The following response is always sent:
      * (DWORD) Server code. The client will
      * calculate the checksum of the auth password and the
      * server code using the BNLS Checksum Algorithm, described
      * in the appendix at the bottom of this document. The
      * result is sent in BNLS_AUTHORIZEPROOF (0x0f).
      *
      * If the bot ID sent in BNLS_AUTHORIZE (0x0e) did not
      * exist, then this message is still sent, as backwards
      * compatibility with the previous version of BNLS, which
      * required authorization.
      */
    BNLSUsername = in.removeNTString();
    if (Constants.displayParseInfo) Out.info("JBLS", ">>> BNLS Bot ID: " + BNLSUsername);
    if (botIds == null) botIds = new Hashtable<String, Integer>(5);
    
    Integer i = (Integer) botIds.get(BNLSUsername.toLowerCase());
    if (i == null) i = new Integer(0);
    i = new Integer(i.intValue() + 1);
    botIds.put(BNLSUsername.toLowerCase(), i);
	
    BNLSServerCode = Math.abs(new Random().nextInt());
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_AUTHORIZE);
    if (Constants.requireAuthorization)
      BNLSPassword = BNLSlist.GetPassword(BNLSUsername);
    reply.addDWord(BNLSServerCode);
    return reply;
  }
  private OutPacketBuffer onAuthorizeProof(InPacketBuffer in) throws BNLSException{
    /* BNLS_AUTHORIZEPROOF (0x0f)
      * --------------------------
      * This is sent to the server when receiving the status code
      * in BNLS_AUTHORIZE (0x0e).
      * (DWORD) Checksum.
      * (DWORD) Client host IP
      * Response:
      * ---------
      * If the client sent a valid account name, but a wrong password
      * checksum, then BNLS disconnects the client.
      * If the client sent an invalid account name, or a valid account
      * name with a correct password checksum, the following response
      * is sent:
      * (DWORD) Status code.
      *     The following status codes are defined:
      *     #define STATUS_AUTHORIZED (0x00)
      *     #define STATUS_UNAUTHORIZED (0x01)
      * STATUS_AUTHORIZED (0x00) means the login was performed as a
      * registered account. STATUS_UNAUTHORIZED (0x01) means an
      * anonymous login was performed.
      */
    int checksum = in.removeDWord();
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_AUTHORIZEPROOF);
    if ((BNLSPassword != null) && (BNLSlist.BNLSChecksum(BNLSPassword, BNLSServerCode) == checksum)){
      reply.addDWord(0x00);
      authorized = true;
      if (Constants.displayParseInfo) Out.info("JBLS", ">>> BNLS Password Verified");
    }else{
	  if (authorized || (!Constants.requireAuthorization)){
        reply.addDWord(0x00);
        if (Constants.displayParseInfo) Out.info("JBLS", ">>> BNLS ID Anonymous Login");
      }else
        throw new BNLSException("Incorrect Pass >>> Close Connection ");
    }
    reply.addBytes(this.connection.address.getAddress());
	if(Controller.stats != null) Controller.stats.onBotLogin(this.BNLSUsername, this.connection.IP);
    return reply;
  }
  private OutPacketBuffer onRequestVersionByte(InPacketBuffer in){
    /* BNLS_REQUESTVERSIONBYTE (0x10)
      * ------------------------------
      * This message requests the latest version byte for a given
      * product. The version byte is sent to Battle.net in SID_AUTH_INFO (0x50).
      * (DWORD) Product ID.
      * Response:
      * ---------
      * (DWORD) On failure (invalid product ID), this is 0.
      *         On success, this is equal to the requested product ID.
      * (DWORD) Latest version byte for specified product. If the
      *         previous DWORD is 0, this DWORD is not included in the message.
      */
    int prod = in.removeDWord();
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_REQUESTVERSIONBYTE);
    int vByte = HashMain.getVerByte(prod);
    if(vByte == 0)
      reply.addDWord(0);
    else{
      reply.addDWord(prod);
      reply.addDWord(vByte);
    }
    return reply;
  }
  private OutPacketBuffer onVerifyServer(InPacketBuffer in){
   /* BNLS_VERIFYSERVER (0x11) 
	* ------------------------
     * This messages verifies a server's signature, which is
     * based on the server's IP. The signature is optional
     * (currently sent only with Warcraft 3), and is sent in
     * SID_AUTH_INFO (0x50).
     * (DWORD) Server's IP.
     * (128 bytes) Signature.
     * Response: ---------
     * (BOOL) Success. (If this is TRUE, the signature matches
     * the server's IP - if this is FALSE, it does not.)
     */
    byte []serverIp = in.removeBytes(4);
    byte []serverSig = in.removeBytes(128);
    boolean result = SRP.checkServerSignature(serverSig, serverIp);
    OutPacketBuffer reply = new OutPacketBuffer((byte) 0x11);
    reply.add(result ? 1 : 0);
    return reply;
  }
  private OutPacketBuffer onReserveServerSlots(InPacketBuffer in){
    /* BNLS_RESERVESERVERSLOTS (0x12)
      * ------------------------------
      *
      * This message reserves a number of slots for concurrent
      * NLS checking operations. No other NLS checking messages
      * can be sent before this message has been sent. This
      * message cannot be sent more than once per connection.
      *
      * (DWORD) Number of slots to reserve.
      * BNLS may limit the number of slots to a reasonable value.
      * Response: ---------
      * (DWORD) Number of slots reserved.
      *
      * This may be equal to the number of slots requested,
      * although it does not necessarily have to be the same
      * value. Valid slot indicies are in the range of [0, Number
      * of slots reserved - 1]. Each slot stores state
      * information about a NLS checking operation. A logon
      * checking session must be finished on the same slot on
      * which it was started. If a logon checking session is
      * abandoned before it is completed, no special action is
      * required. Starting a new logon checking session on a slot
      * overwrites all previous state information. A logon
      * checking session cannot be resumed if the connection to
      * BNLS is interrupted before it is completed.
      */
    int askedFor = in.removeDWord();
    if (askedFor < 0)  askedFor = 1;
    if (askedFor > 32) askedFor = 32;
    reservedSRPs = new SRP[askedFor];
	OutPacketBuffer reply = new OutPacketBuffer(BNLS_RESERVESERVERSLOTS);
	reply.addDWord(askedFor);
	return reply;
  }
  private OutPacketBuffer onServerLogonChallenge(InPacketBuffer in){
    /* BNLS_SERVERLOGONCHALLENGE (0x13)
      * --------------------------------
      *
      * This message initializes a new logon checking session and
      * calculates the values needed for the server's reply to
      * SID_AUTH_ACCOUNTLOGON (0x53). BNLS_RESERVESERVERSLOTS
      * (0x12) must be sent before this message to reserve slots
      * for logon checking sessions.
      *
      * (DWORD) Slot index.
      * (DWORD) NLS revision number.
      * (16 DWORDs) Data from account database.
      *    -8DWORDs Salt
      *    -8DWORDs 'v'
      * (8 DWORDs) Data from the client's SID_AUTH_ACCOUNTLOGON (0x53) request.
      *
      * Both the slot indicies and the NLS revision number follow
      * their respective conventions introduced earlier in this
      * document. The account database data is first received
      * from the client's SID_AUTH_ACCOUNTCREATE (0x04) message.
      * This information must be stored by the server's account
      * database for logon checking. If the account database data
      * is invalid, then the logon checking session will not
      * succeed. This message initializes a slot with all the
      * information required for it to operate, including the NLS
      * revision. Although BNLS supports switching the NLS
      * revision of a given slot, it can respond to requests
      * slightly faster if the same NLS revision is used for the
      * same slots in a given connection.
      *
      * Response: ---------
      *
      * (DWORD) Slot index.
      * (16 DWORDs) Data for the server's SID_AUTH_ACCOUNTLOGON (0x53) response.
      *
      * The slot index is returned since individual operations
      * may be returned in a different order than they are
      * requested. This message can also be used to calculate the
      * server's SID_AUTH_ACCOUNTCHANGE (0x55) response. Simply
      * substitute the SID_AUTH_ACCOUNTLOGON (0x53) data with the
      * SID_AUTH_ACCOUNTCHANGE (0x55) data.
      */
    int slot = in.removeDWord();
    int NLS = in.removeDWord();
    if (SRPs < slot) return null;
    byte[] salt = in.removeBytes(SRP.BIGINT_SIZE);
    byte[] v = in.removeBytes(SRP.BIGINT_SIZE);
    reservedSRPs[slot] = new SRP(in.removeBytes(SRP.BIGINT_SIZE));
    reservedSRPs[slot].set_NLS(NLS);
	
    byte[] B = reservedSRPs[slot].get_B(v);
    reservedSRPs[slot].set_B(B);
	
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_SERVERLOGONCHALLENGE);
    reply.addDWord(slot);
    for (int Y = 0; Y < SRP.BIGINT_SIZE; Y++)
      reply.addByte(salt[Y]);
	for (int Y = 0; Y < SRP.BIGINT_SIZE; Y++)
	  reply.addByte(B[Y]);
    return reply;
  }
  private OutPacketBuffer onServerLogonProof(InPacketBuffer in){
    /* BNLS_SERVERLOGONPROOF (0x14)
      * ----------------------------
      * This message performs two operations. First, it checks if
      * the client's logon was successful. Second, it calculates
      * the data for the server's reply to
      * SID_AUTH_ACCOUNTLOGONPROOF (0x54). If this data is not
      * correct, then the client will not accept the logon
      * attempt as valid.
      * (DWORD) Slot index.
      * (5 DWORDs) Data from the client's SID_AUTH_ACCOUNTLOGONPROOF (0x54) request.
      * (STRING) The client's account name.
      * Response: ---------
      * (DWORD) Slot index.
      * (BOOL) Success.
      *       (If this is TRUE, then the client's logon information
      *        was valid. Otherwise, if it is FALSE, then the client's
      *        logon information was invalid, and the logon request must
      *        be denied.)
      * (5 DWORDs) Data for the server's SID_AUTH_ACCOUNTLOGONPROOF (0x54) response.
      *
      * After this message is received, the logon checking
      * sequence for a particular logon session is complete. This
      * message can also be used to calculate the server's
      * SID_AUTH_ACCOUNTCHANGEPROOF (0x56) response, and check
      * the client's change password request. Simply substitute
      * the SID_AUTH_ACCOUNTLOGONPROOF (0x54) data with the
      * SID_AUTH_ACCOUNTCHANGEPROOF (0x56) data
      */
    int slot = in.removeDWord();
    byte[] M1 = in.removeBytes(SRP.SHA_DIGESTSIZE);
    if (SRPs < slot) return null;
	byte[] M2 = reservedSRPs[slot].getM2(reservedSRPs[slot].get_A(), reservedSRPs[slot].get_B());
	OutPacketBuffer reply = new OutPacketBuffer(BNLS_SERVERLOGONPROOF);
	reply.addDWord(slot);
	reply.addDWord((equal(M1, M2) ? 0x01 : 0x00));
	for(int Y = 0; Y < 5; Y++)
	  reply.addDWord(M1[Y]);
	return reply;
  }
  private OutPacketBuffer onVersionCheckEX(InPacketBuffer in){
    /* BNLS_VERSIONCHECKEX (0x18)
      * --------------------------
      * This message performs two operations.
      * First, will request a fast version check.
      *   Now works with all products.
      * Second, it will request the current version code
      *   for the given product (eliminating the need for BNLS_REQUESTVERSIONBYTE).
      * (DWORD) Product ID.*
      * (DWORD) Version DLL digit in the range 0-7. (For example, for IX86Ver1.mpq this is 1)
      * (DWORD) Flags.**
      * (DWORD) Cookie.
      * (String) Checksum formula.
      *  ** The flags field is currently reserved and must be set to zero or you will be disconnected.
      *
      *  Response:
      *  ---------
      *  (BOOL) Success (TRUE if successful, FALSE otherwise).
      *         If this is FALSE, the next DWORD is the provided cookie, following which the message ends.
      *  (DWORD) EXE Version.
      *  (DWORD) Checksum.
      *  (String) EXE Info.
      *  (DWORD) Cookie.
      *  (DWORD) VerByte.
      */
    int prod = in.removeDWord();
    int archiveNum = in.removeDWord();
    in.removeDWord();
    int cookie = in.removeDWord();
    String formula = in.removeNTString();
    String archive = "ver-IX86-" + archiveNum + ".mpq";
    if(prod == Constants.PRODUCT_STARCRAFT       ||
       prod == Constants.PRODUCT_BROODWAR        ||
       prod == Constants.PRODUCT_WAR2BNE         ||
       prod == Constants.PRODUCT_JAPANSTARCRAFT  ||
       prod == Constants.PRODUCT_DIABLO          ||
       prod == Constants.PRODUCT_DIABLOSHAREWARE ||
       prod == Constants.PRODUCT_STARCRAFTSHAREWARE)
       archive = "lockdown-IX86-" + PadString.padNumber(archiveNum, 2) + ".mpq";
    
    CheckrevisionResults revision = HashMain.getRevision(prod, formula, archive, (long)0);
    int versionByte = HashMain.getVerByte(prod);
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_VERSIONCHECKEX);
    if(revision == null || versionByte == 0){
      reply.addDWord(0);
      reply.addDWord(cookie);
      return reply;
    }
    reply.addDWord(1);
    reply.addDWord(revision.getVersion());
    reply.addDWord(revision.getChecksum());
    reply.addBuffer(revision.getInfo());
    reply.addDWord(cookie);
    reply.addDWord(versionByte);
    if(Controller.stats != null) Controller.stats.onCheckRevision(this.BNLSUsername, this.connection.IP, archive, prod);
    return reply;
  }  
  private OutPacketBuffer onVersionCheckEX2(InPacketBuffer in){
    /* BNLS_VERSIONCHECKEX2 (0x1A)
      * ---------------------------
      * This message performs two operations.
      * First, will request a fast version check. Now works with all products.
      * Second, it will request the current version code for the given product
      * (eliminating the need for BNLS_REQUESTVERSIONBYTE).
      * This message does not require the client to perform any parsing on the version
      * check MPQ filenames. Instead, the full file name and timestamp are sent to the server.
      * (DWORD) Product ID.
      * (DWORD) Flags.**
      * (DWORD) Cookie.
      * (ULONGLONG) Timestamp for version check archive.
      * (String) Version check archive filename.
      * (String) Checksum formula.
      * ** The flags field is currently reserved and must be set to zero or you will be disconnected.
      * Response:
      * ---------
      * (BOOL) Success (TRUE if successful, FALSE otherwise).
      *        If this is FALSE, the next DWORD is the provided cookie, following which the message ends.
      * (DWORD) Version.
      * (DWORD) Checksum.
      * (String) Version check stat string.
      * (DWORD) Cookie.
      * (DWORD) VerByte
      */
    int prod = in.removeDWord();
    in.removeDWord();
    int cookie = in.removeDWord();
    long fileTime = in.removeLong();
    String archive = in.removeNTString();
    String formula = in.removeNTString();
                  
    CheckrevisionResults revision = HashMain.getRevision(prod, formula, archive, fileTime);
    int versionByte = HashMain.getVerByte(prod);
    OutPacketBuffer reply = new OutPacketBuffer(BNLS_VERSIONCHECKEX2);
    if(revision == null || versionByte == 0){
      reply.addDWord(0);
      reply.addDWord(cookie);
      return reply;
    }
    reply.addDWord(1);
    reply.addDWord(revision.getVersion());
    reply.addDWord(revision.getChecksum());
    reply.addBuffer(revision.getInfo());
    reply.addDWord(cookie);
    reply.addDWord(versionByte);
	if(Controller.stats != null) Controller.stats.onCheckRevision(this.BNLSUsername, this.connection.IP, archive, prod);
    return reply;
  }
  private OutPacketBuffer onUnknown(InPacketBuffer in){
    Out.error("JBLS Parse", "Unhandled packet 0x" + ((in.getPacketID() & 0xF0) >> 4) + "" +
             Integer.toString((in.getPacketID() & 0x0F) >> 0, 16) );
    Out.debug("JBLS Parse", "Packet Data:\r\n" + in.toString());
	return null;
  }
  
}// end of BNLS Parse class
