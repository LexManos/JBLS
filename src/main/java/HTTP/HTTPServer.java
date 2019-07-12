package HTTP;
import java.net.*;
import util.*;
import java.io.IOException;

public class HTTPServer extends Thread{
    final static String CRLF = "\r\n";
	ServerSocket server = null;
	boolean listening = false;

	public HTTPServer(){
		Out.info("HTTP", "Server thread created.");
	}

	public void run(){
	  try{
	    server = new ServerSocket(Constants.HTTPPort);
	  }catch (BindException e) {
	    Out.error("HTTP", "Could not bind port " + Constants.HTTPPort + ". HTTP server is disabled.");
		return;
	  }catch (IOException e){
	    Out.error("HTTP","Could not create socket (Port: " + Constants.HTTPPort + ")  - Shutting down the HTTP server. \r\n"+e.toString());
	    return;
	  }
	  listening=true;
	  Out.println("HTTP","Server socket opened on port " + Constants.HTTPPort);
	  while (listening) {
	    try {
		  Out.info("HTTP","Listening for new Connection...");
		  Socket inSocket=server.accept();

		  HTTPParse bConnection = new HTTPParse(inSocket);
		  bConnection.start();
		}catch (IOException e) {
  		  Out.error("JBLS","Could not Accept Connection " + e.toString());
		  listening=false;
		  break;
		}
	  }
	  Out.info("HTTP","Server Thread Terminated");
	}
	public void destroyAllConnections(){}

	public void closeSocket(){
	  listening=false;
	  try{
		if(!server.isClosed())
	  	  server.close();
	  }catch(IOException e){
		Out.error("HTTP","Error Closing Socket: "+e.toString());
	  }
	}

	public static String buildHeader(int status, String mimeType, long contentLong){
	  String toSend  = buildHeader(status);
		     toSend += "Content-Length: " + String.valueOf(contentLong) + CRLF;
		     toSend += "Content-Type: " + mimeType + CRLF;
	  return toSend;
	}

	public static String buildHeader(int status) {
		String message, toSend;
		switch(status) {
			case 200:message = "200 OK"; break;
			case 202:message = "202 Accepted"; break;
			case 300:message = "300 Ambiguous"; break;
			case 301:message = "301 Moved Permanently"; break;
			case 400:message = "400 Bad Request"; break;
			case 401:message = "401 Denied"; break;
			case 403:message = "403 Forbidden"; break;
			case 404:message = "404 Not Found"; break;
			case 405:message = "405 Bad Method"; break;
			case 413:message = "413 Request Entity Too Large"; break;
			case 415:message = "415 Unsupported Media"; break;
			case 501:message = "501 Not Implemented"; break;
			case 502:message = "502 Bad Gateway"; break;
			case 504:message = "504 Gateway Timeout"; break;
			case 505:message = "505 HTTP Version Not Supported"; break;
			default: message = "500 Internal Server Error";
		}
		toSend  = "HTTP/1.1 " + message + CRLF;
		toSend += "Server: JBLS/0.1" + CRLF;
		if(status == 501)
		  toSend += "Allow: GET" + CRLF;
		toSend += "Cache-Control: no-cache, must-revalidate" + CRLF;
		toSend += "Connection: close" + CRLF;
		return toSend;
	}
}
