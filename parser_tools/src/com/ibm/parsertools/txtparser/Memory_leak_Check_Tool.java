/**
 * 
 */
package com.ibm.parsertools.txtparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Steven
 *
 */

class Info
{
	private String address;
	private String lineString;
	public String getAddress()
	{
		return address;
	}
	public void setAddress(String address)
	{
		this.address = address;
	}
	public String getLineString()
	{
		return lineString;
	}
	public void setLineString(String lineString)
	{
		this.lineString = lineString;
	}
	
	public boolean equals(Object anObject)
	{
		return this.address.equals(((Info)anObject).getAddress());
	}
	
	public int hashCode()
	{
		return this.address.hashCode();
	}
	
}
public class Memory_leak_Check_Tool
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		HashSet<Info> set = new HashSet<Info>();
		File file = null;
		BufferedReader reader = null;
		
		
		file = new File(args[0]);
		try
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		}
		catch (FileNotFoundException e)
		{
			System.err.println("File " + file.getName() + " NOT found!");
		}
		try
		{
			String lineString = null;
			Pattern namePattern = Pattern.compile("[A-F|a-f|0-9]{8}");
			//Pattern defectPattern = Pattern.compile("[A-Z|a-z][A-Z|a-z][0-9]{6}");

			while ((lineString = reader.readLine()) != null)
			{
				Matcher nameMatcher = namePattern.matcher(lineString);
				//Matcher defectMatcher = defectPattern.matcher(lineString);
				if (nameMatcher.find())
				{
					//System.out.println(lineString);
					String address = nameMatcher.group();
					Info info = new Info();
					info.setAddress(address);
					info.setLineString(lineString);
					if (!set.contains(info))
					{
						set.add(info);
					}
					else 
					{
						set.remove(info);
					}
					
				}
			}
			reader.close();
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		for (Iterator iterator = set.iterator(); iterator.hasNext();)
		{
			Info info = (Info) iterator.next();
			System.out.println(info.getLineString());
			//System.out.println(engineerInfo.getNameString());
		}
	}

}
