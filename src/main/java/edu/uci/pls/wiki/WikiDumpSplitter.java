package edu.uci.pls.wiki;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Lu Fang
 * 
 * We can download wiki dump at http://dumps.wikimedia.org/enwiki/latest/
 */
public class WikiDumpSplitter {
	public static String baseDir;
	public static String fileBase = "";
	public static int currentId = 0;
	public static long sizeConstraint = 256 * 1024 * 1024;	//Unit is Byte
	/**
	 * @return true, if succeed, otherwise return false
	 */
	public static boolean split(String originWikiDumpXml){
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(originWikiDumpXml));
			String splitFileName = baseDir + "/" + fileBase + "-" + currentId;
			BufferedWriter bw = new BufferedWriter(new FileWriter(splitFileName));
			StringBuffer sb = new StringBuffer();
			long accumulatedSize = 0;
			
			boolean pageStart = false;
			
			while (true){
				String line = br.readLine();
				if (line == null){
					break;
				}
				line = line.trim();
				if (line.equals("<page>")){
					if (pageStart){
						//Parsing error
						System.err.println("Something is wrong with <page>!");
					}
					sb.append(line);
					sb.append("\n");
					pageStart = true;
				}else if (line.equals("</page>")){
					if (!pageStart){
						//Parsing error
						System.err.println("Something is wrong with </page>!");
					}else{
						sb.append(line);
						sb.append("\n");
						if (accumulatedSize + sb.length() <= sizeConstraint){
							//Still write to the current file
							bw.write(sb.toString());
						}else{
							//Write to a new file
							bw.close();
							++ currentId;
							splitFileName = baseDir + "/" + fileBase + "-" + currentId;
							bw = new BufferedWriter(new FileWriter(splitFileName));
							bw.write(sb.toString());
							accumulatedSize = 0;
						}
						accumulatedSize += sb.length();
						sb.delete(0, sb.length());
					}
					pageStart = false;
				}else{
					if (pageStart){
						sb.append(line);
						sb.append("\n");
					}
				}
				
			}
			
			br.close();
			bw.close();	//If there is no </page>, we ignore the rest in sb
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static void main(String args[]){
		if (args.length < 2){
			System.err.println("Usage edu.uci.pls.wiki.WikiDumpSplitter input-path output-path [size-byte-constraint]");
		}
		String originWikiDumpXml = args[0];
		baseDir = args[1];
		
		File inputFile = new File(originWikiDumpXml);
		if (!inputFile.exists()){
			System.err.println("The input file does not exist! " + originWikiDumpXml);
			System.exit(1);
		}
		
		if (inputFile.isDirectory()){
			System.err.println("The input file is a directory! " + originWikiDumpXml);
			System.exit(1);
		}
		
		File dir = new File(baseDir);
		if (!dir.exists()){
			dir.mkdirs();
		}
		
		if (args.length > 2){
			try{
				sizeConstraint = Long.parseLong(args[2]);
			}catch(Exception e){
				System.err.println("please input an integer as the size constraint!");
			}
		}
		
		int namingMode = 0;
		if (namingMode == 0){
			int lastSlashIndex = originWikiDumpXml.lastIndexOf('/');
			if (lastSlashIndex < 0){
				fileBase = originWikiDumpXml;
			}else{
				fileBase = originWikiDumpXml.substring(lastSlashIndex + 1);
			}
		}else{
			fileBase = "split";
		}
		split(originWikiDumpXml);
	}
}
