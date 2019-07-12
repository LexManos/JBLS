package util;
import java.io.*;
import java.util.zip.*;
import java.util.Enumeration;

public class ZIP {
	static final int BUFFER = 2048;
	public static void main(String[] args) 
	{
		String[] files = new String[] {
			"war3.exe", "game.dll", "storm.dll"};
		System.out.println(ZIP.CreateZip(files, "WAR3.zip"));
		System.out.println(ZIP.ExtractZip("WAR3.zip", "./w/"));
	}
	
	public static String CreateZip(String[] filesToZip, String zipFileName){

		byte[] buffer = new byte[18024];

		try {

			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
			out.setLevel(Deflater.BEST_COMPRESSION);
			for (int i = 0; i < filesToZip.length; i++) {
				FileInputStream in = new FileInputStream(filesToZip[i]);
				String fileName = null;
				for (int X = filesToZip[i].length() - 1; X >= 0; X--){
					if(filesToZip[i].charAt(X) == '\\' || filesToZip[i].charAt(X) == '/'){
						fileName = filesToZip[i].substring(X+1);
						break;
					}else if(X == 0)
						fileName = filesToZip[i];
				}
         		out.putNextEntry(new ZipEntry(fileName));
				int len;
				while ((len = in.read(buffer)) > 0)
					out.write(buffer, 0, len);
				out.closeEntry();
         		in.close();
			}
       		out.close();
	     }catch (IllegalArgumentException e){
	       	return "Failed to create zip: " + e.toString();
	     }catch (FileNotFoundException e){
	       	return "Failed to create zip: " + e.toString();
	     }catch (IOException e){
	     	return "Failed to create zip: " + e.toString();
	     }
     return "Success";
   }
	
	public static String ExtractZip(String zipName, String outFolder){
		try {
			File sourceZipFile = new File(zipName);
			File outDirectory = new File(outFolder);
			ZipFile zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
			
			Enumeration zipFileEntries = zipFile.entries();
			while (zipFileEntries.hasMoreElements()){
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
	
				String currentEntry = entry.getName();
				for (int X = currentEntry.length() - 1; X >= 0; X--){
					if(currentEntry.charAt(X) == '\\' || currentEntry.charAt(X) == '/'){
						currentEntry = currentEntry.substring(X+1);
						break;
					}
				}
				File destFile = new File(outDirectory, currentEntry);
				if (destFile.getParentFile() != null)
					destFile.getParentFile().mkdirs();
				if (!entry.isDirectory()){
					BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
					int currentByte;
					byte data[] = new byte[BUFFER];
					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
					while ((currentByte = is.read(data, 0, BUFFER)) != -1)
						dest.write(data, 0, currentByte);
					dest.flush();
					dest.close();
					is.close();
				}
			}
			zipFile.close();
			return "success";
		}catch (IOException e){
			return "Failed to extract zip: " + e.toString();
		}
	}
}
