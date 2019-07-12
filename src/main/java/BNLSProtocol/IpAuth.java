/*
 * Created on Dec 18, 2004
 */
package BNLSProtocol;
import util.Constants;
import util.Out;
import util.Ini;
/**
 * IpAuth Class
 *
 * This Class Stores the IP Authorization/Banning list
 * It Keeps Track of Who Connects and when, and performs any necessary
 * IPBanning
 *
 * This Class and all members are static, in order to be accessible
 * to all connection threads
 *
 */
public class IpAuth {

	/* Constants for the IP Authorization Status*/

	public static final int IPNORESTRICTIONS=0;//All connections are completly unrestricted
	public static final int IPBANNING=1;       //Enables IPBanning of IPs
	public static final int IPRESTRICTED=2;    //Only allows Authorized IPs to connect
	public static final int IPLOCALONLY=3;     //Only allows Local IPs to connect (prefix 10, 192, 127

	/******************************************************
	 *@param IP - IP address to be checked, as a string   *
	 *@return boolean whether the IP is allowed to connect*
	 *@SuppressWarnings("fallthrough")                    *
	 ******************************************************/
	public static boolean checkAuth(String IP)
	{
		int thisIpStatus=Integer.parseInt(Ini.ReadIni("./ips.ini", IP, "Status", "-1"));
    if(thisIpStatus != -1){
		  int count=Integer.parseInt(Ini.ReadIni("./ips.ini", IP, "Connection Attempts", "0"));
      count++;
      Ini.WriteIni("./ips.ini", IP, "Last Connection", Out.getDatestamp());
      Ini.WriteIni("./ips.ini", IP, "Connection Attempts", count+"");
    }
   
		boolean allow=false;//whether we successfully passed this object
		
		switch(Constants.ipAuthStatus) //make choice based on our status
		{
			// Everything passes
			case IPNORESTRICTIONS:
				allow=true;
				break;

			// Check for banned IPs
			case IPBANNING:
				if(thisIpStatus!=IPBANNING) allow=true;
				break;

			case IPRESTRICTED:
				allow = (thisIpStatus==IPRESTRICTED) ? true : false;
				String comp=IP.substring(0,3);
				if(comp.equals("127")||comp.equals("192")||comp.equals("10."))
					allow=true;
                break;

			case IPLOCALONLY:
				comp=IP.substring(0,3);//get first 3 chars of the IP
				if(comp.equals("127")||comp.equals("192")||comp.equals("10."))
					allow=true;
				break;
		}// End Switch Statement
		return allow;
	}
  public static String getList(){
    String[] ips = Ini.headers("./ips.ini");
    StringBuffer s = new StringBuffer();
    for(int x = 0; x<ips.length; x++)
      s.append(getIpString(ips[x])).append("\r");
    return s.toString();
  }
  public static String getIpString(String IP){
  StringBuffer s = new StringBuffer();
    s.append("IP: ").append(IP);
    s.append(" Status: ");
    int status = Integer.parseInt(Ini.ReadIni("./ips.ini", IP, "Status", "0"));
    s.append(getStatus(status));
    s.append(" Connection Attempts: ");
    s.append(Ini.ReadIni("./ips.ini", IP, "Connection Attempts", "0"));
    s.append(" Last Connection: ");
    s.append(Ini.ReadIni("./ips.ini", IP, "Last Connection", "0"));
    return s.toString();
  }
  public static String getIpStatus(String IP){
    int status = Integer.parseInt(Ini.ReadIni("./ips.ini", IP, "Status", "0"));
    return getStatus(status);
  }
  public static String getStatus(int status){
    switch (status){
      case IPNORESTRICTIONS: return "Restricted";
      case IPBANNING: return "Banned";
      case IPRESTRICTED: return "No Restrictions";
      default: return "Unknown (" + status + ")";
    }
  }
  public static void setIpStatus(String IP, int status){
    Ini.WriteIni("./ips.ini", IP, "Status", status+"");
  }
}
