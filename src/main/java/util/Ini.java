package util;

import java.io.*;
import java.util.*;

public class Ini{
  //private Hashtable<String, String> data = new Hashtable<String, String>();
  private Hashtable<String, Properties> data = new Hashtable<String, Properties>();
  String file = null;
  
  public Ini(String fileName){
    file = fileName;
    if(new File(fileName).exists()){
		  try{
			  BufferedReader inputStream = new BufferedReader(new FileReader(fileName));
			  String line = "";
			  String header = null;
        Properties section = null;
			  while ((line = inputStream.readLine()) != null){
          if(line.length() > 0){
				    if(line.substring(0,1).equals("[") && line.substring(line.length()-1, line.length()).equals("]")){
				      if(header != null) data.put(header, section);
				      header = line.toLowerCase();
				      section = new Properties();
				    }else{
              int index = line.indexOf("=");
              if(index > 0){
                String prop = line.substring(0, index);
                String valu = line.substring(index+1);
                section.setProperty(prop.toLowerCase(), valu);
                System.out.println(prop + "     =    " + valu);
              }
			  	  }
          }
		    }
		    if(header != null) data.put(header, section);
		  }catch(FileNotFoundException e){
		  }catch(IOException ex){}
    }
  }

  public void save(){
    createIfNotExist(file);
    try{
      FileOutputStream out = new FileOutputStream(file, false);
      for(Enumeration keys = data.keys(); keys.hasMoreElements();){
        String header = keys.nextElement().toString();
        out.write((header+System.getProperty("line.separator")).getBytes());
        //System.out.println(header);
        Properties section = data.get(header);
        for(Enumeration props = section.propertyNames(); props.hasMoreElements();){
          String prop = props.nextElement().toString();
          String value = section.getProperty(prop);
          out.write((prop + "=" + value+System.getProperty("line.separator")).getBytes());
        }
      }
      out.close();
		}catch(FileNotFoundException e){
		}catch(IOException ex){}
  }
  
  
  public static String[] headers(String FN){
   if(new File(FN).exists()){
		  try{
			  BufferedReader inputStream = new BufferedReader(new FileReader(FN));
			  String line = "";
        StringBuffer headers = new StringBuffer();
			  while ((line = inputStream.readLine()) != null){
          if(line.length() > 0){
				    if(line.substring(0,1).equals("[") && line.substring(line.length()-1, line.length()).equals("]"))
              headers.append(line.substring(1, line.length()-1)).append("\r\n");
          }
		    }
        return headers.toString().split("\r\n");
		  }catch(FileNotFoundException e){
		  }catch(IOException ex){}
    }
    return null;
  }
  
	public static boolean iniHasHeader(String FN,String Header){
		String var="";
		if (!(new File(FN)).exists()) return false;
		try{
			BufferedReader inputStream = new BufferedReader(new FileReader(FN));
			while ((var = inputStream.readLine()) != null)
				if (var.toLowerCase().equals("[" + Header.toLowerCase() + "]")) return true;
		}catch(FileNotFoundException e){
		}catch(IOException ex){}
		return false;
	}

	public static String ReadIni(String FN,String Header,String Setting,String Default){
		boolean fndHdr=false;
		String var="";
		createIfNotExist(FN);
		try{
			BufferedReader inputStream = new BufferedReader(new FileReader(FN));
			while ((var = inputStream.readLine()) != null){
				if (var.toLowerCase().equals("[" + Header.toLowerCase() + "]"))
					fndHdr=true;
				else if (fndHdr==true && var.substring(0, 1).equals("[")==true) 
					break; 
				else if( var.length() >= Setting.length()+1 && fndHdr==true && var.toLowerCase().substring(0, Setting.length()).equals(Setting.toLowerCase()))
					return (var.length() == Setting.length()+1) ? Default : var.substring(Setting.length()+1,var.length()); 
			}
		}catch(FileNotFoundException e){
		}catch(IOException ex){}
		return Default;
	}

	public static void WriteIni(String FN,String Header,String Setting,String Value){
		StringBuffer StringB = new StringBuffer();
		String tmpFile;
		String var="";
		boolean doneIt=false;
		boolean fndHdr=false;
		createIfNotExist(FN);
		try{
			BufferedReader inputStream = new BufferedReader(new FileReader(FN));
			while ((var = inputStream.readLine()) != null){
				if (fndHdr==false) StringB.append(var + "\n");
				else if ((var.length() >= Setting.length()+1) && (var.toLowerCase().substring(0, Setting.length()+1).equals(Setting.toLowerCase() +"=")))
					fndHdr=false;
				else
					StringB.append(var + "\n");
				if (var.toLowerCase().equals("[" + Header.toLowerCase() + "]")){
					doneIt=true; 
					fndHdr=true; 
					StringB.append(Setting + "=" + Value + "\n");
				}
			}
			inputStream.close();
			if (doneIt==false) StringB.append("[" + Header +"]\n" + Setting +"="+ Value +"\n");
	
			tmpFile=StringB.toString ();
			RandomAccessFile outp = new RandomAccessFile(FN, "rw");
			outp.writeBytes(tmpFile);
			outp.close();
		}catch(FileNotFoundException e){
		}catch(IOException ex){}
	}

    private static void createIfNotExist(String fn){
    	File file = new File(fn);
    	try{
    		if (!file.exists()) file.createNewFile();	
    	}catch(IOException e){}
    }
    
}