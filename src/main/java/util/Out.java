/*
 * Created on Feb 20, 2005
 */
package util;

/**
 *
 * This class controls output from the server, specifying where it goes
 *
 * Currently supports 4 "types" of output, but the system I use now sucks
 * and the formatting is ugly.
 */

import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class Out {

	private static PrintStream outStream;
	private static PrintWriter logWriter;
	
	private static String logFile = null;

    static
    {
        //System.out.println(new Date() + "");
    }

	/**
	 @param text: Text to be send to the output stream directly(no formatting)*/
	public static void print(String text)
	{
		outStream.print(text);
		
		openLogFile();
		if (Constants.enableLogging && (logWriter != null)) {
			logWriter.print(text);
		}
	}
	
	public static void println(String text)
	{
		String fullText = getTimestamp() + text;
		outStream.println(fullText);
		
		openLogFile();
		if (Constants.enableLogging && (logWriter != null)) {
			logWriter.println(fullText);
		}
	}

	/**@param source: source of the info
	//@param text:text to show*/
	public static void println(String source, String text)
	{
		println("{" + source + "} " + text);
	}

	/**Displays errors
	//@param source: source of the info
	//@param text: text to show*/
	public static void error(String source, String text)
	{
		println(source + " - Error", text);
	}

	/**Displays debug information, if wanted
	//@param source - source of the info
	//@param text -text to show*/
	public static void debug(String source, String text)
	{
		if(Constants.debugInfo)
			println(source + " - Debug", text);
	}

	/**Displays "info"
	@param source - source of the info
	@param text -text to show*/
	public static void info(String source, String text)
	{
		println(source, text);
	}

	/**Sets the output stream for the information to be displayed to.
	Can be set to system.out, admin output stream, file logging, etc..
	@param s
	PrintStream to send information to.
	*/
	public static void setOutputStream(PrintStream s)
	{
		outStream=s;
		if(outStream==null)
			setDefaultOutputStream();
		else
			openLogFile();
	}

	/**Sets the default PrintStream, currently system.out*/
	public static void setDefaultOutputStream()
	{
		outStream=System.out;
		
		openLogFile();
	}
	
	private static void openLogFile()
	{
		if (!Constants.enableLogging)
			return;
		
		String filePath = Constants.LogFilePath + getFileDate() + ".log";
		if (logFile == null || !filePath.equals(logFile)) {
			logFile = filePath;
			
			purgeOldLogs();
			
			// Close the current file.
			if (logWriter != null) logWriter.close();
			
			try {
				// Create log folder if it doesn't exist
				new File(Constants.LogFilePath).mkdir();
				
				// Open the new log file
				logWriter = new PrintWriter(new FileWriter(logFile, true), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void purgeOldLogs()
	{
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date today = new Date();
		Date fileDate;
		
		File logDir = new File(Constants.LogFilePath);
		File[] logFiles = logDir.listFiles();
		if (logFiles != null) {
			for (File file : logFiles) {
				String fileName = file.getName();
				try {
					fileDate = df.parse(fileName.substring(0, fileName.length()- 4));
					
					long diff = (today.getTime() - fileDate.getTime());
					if ((diff / (1000 * 60 * 60 * 24)) > Constants.logKeepDuration)
						file.delete();
					
				} catch (ParseException e) { }
			}
		}
	}

	/**
	 * Simply displays a nicely formatted timestamp.
	 *
	 * @author Ron
	 * -Fool Change: Moved in here instead of own TimeStamp Class
	 *     Don't really know why, but I did
	 */
	public static String getTimestamp()
    {
        Calendar c = Calendar.getInstance();

        StringBuffer s = new StringBuffer();
        s.append('[');
        s.append(getTime());
        s.append("] ");

        return s.toString();
    }
	public static String getDatestamp()
    {
        Calendar c = Calendar.getInstance();
        StringBuffer s = new StringBuffer();
        s.append(PadString.padNumber(c.get(Calendar.MONTH) + 1, 2)).append("/");
        s.append(PadString.padNumber(c.get(Calendar.DAY_OF_MONTH), 2)).append("/");
        s.append(c.get(Calendar.YEAR)).append(" ");
        s.append(getTime());

        return s.toString();
    }
	
	public static String getTime()
	{
		Calendar c = Calendar.getInstance();

        StringBuffer s = new StringBuffer();
        s.append(PadString.padNumber(c.get(Calendar.HOUR_OF_DAY), 2)).append(':');
        s.append(PadString.padNumber(c.get(Calendar.MINUTE), 2)).append(':');
        s.append(PadString.padNumber(c.get(Calendar.SECOND), 2)).append('.');
        s.append(PadString.padNumber(c.get(Calendar.MILLISECOND), 3));
        
        return s.toString();
	}
	
	public static String getFileDate()
	{
		Calendar c = Calendar.getInstance();
		
        StringBuffer s = new StringBuffer();
        s.append(c.get(Calendar.YEAR)).append("-");
        s.append(PadString.padNumber(c.get(Calendar.MONTH) + 1, 2)).append("-");
        s.append(PadString.padNumber(c.get(Calendar.DAY_OF_MONTH), 2));
        
        return s.toString();
	}
}
