

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

//import org.apache.catalina.connector.Request;

public class ReadCSVFile
{

	public static void main(String args[])
	{
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please enter the name of your input folder:: ");
		String inputFileName = scanner.next();
		System.out.println("Enter String to be found:");
		String findString = scanner.next();
		System.out.println("Enter String to be replaced:");
		String replaceString = scanner.next();
		
		File path = new File(inputFileName);
	    File [] files = path.listFiles();
	    for (int i = 0; i < files.length; i++)
	    {
	        if (files[i].isFile())
	        { 
	            System.out.println(files[i]);
	            
	    		boolean file_exists = files[i].exists();
	    		if(!file_exists)
	    			System.err.println("No such file exists. Please re enter the file name!! \n ");
	    		getCsvDetails(files[i].toString(),findString,replaceString);
	        }
	    }
		System.out.println("Parsed "+files.length+" files");
		scanner.close();
	}
	
	public static void getCsvDetails(String file,String findString, String replaceString)
	{
		System.out.println("Inside getCsvDetails");
		try
		{
			String filename=file; 
			String thisLine;
			FileInputStream fs=new FileInputStream(filename);
			DataInputStream ds=new DataInputStream(fs);
			ArrayList<String> fileData = new ArrayList<String>();
			int lineCount = -1;
			int recordCount = 0;
			while((thisLine=ds.readLine())!=null)
			{
				
				String str[]=thisLine.split(",");
				StringBuffer modifiedString = new StringBuffer(); 
				recordCount = 0;
				for(int i=0;i<str.length;i++)
				{
					String modifiedStringPiece;
					if(str[i].contains(findString))
					{
						modifiedStringPiece= str[i].replace(findString, replaceString);
						recordCount++;
					}
					else 
					{
						modifiedStringPiece = str[i];
					}
					modifiedString.append(modifiedStringPiece);
					if(i!=str.length-1)
						modifiedString.append(",");
				}
				fileData.add(modifiedString.toString());
				lineCount++;
			
			}
			System.out.println("Modified "+lineCount+" lines with "+recordCount+" occurences in each line" );
			writeRulesToFile(filename,fileData);
		}
		
		    catch(Exception e)
		    {
		    
		    	System.out.println("Excpetion Occured!!!");
		    }
	
		
	}
	public static void writeRulesToFile(String fileName, ArrayList<String> rules)
	{
		try
		{
			File file = new File(fileName);
			file.createNewFile();
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for(String rule: rules)
			{
				bw.write(rule);
				bw.newLine();
			}
			bw.close(); 
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
