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
 * @author Jeff
 *
 */
public class DefectInfoTextParser
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		HashSet<Engineer> set = new HashSet<Engineer>();
		File file = null;
		BufferedReader reader = null;
		for (int i = 0; i < args.length; i++)
		{
			file = new File(args[i]);
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
				Pattern namePattern = Pattern.compile("[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}\\ [0-9]{2}\\:[0-9]{2}");
				Pattern defectPattern = Pattern.compile("[A-Z|a-z][A-Z|a-z][0-9]{6}");
				Engineer engineer = null;
				while ((lineString = reader.readLine()) != null)
				{
					Matcher nameMatcher = namePattern.matcher(lineString);
					Matcher defectMatcher = defectPattern.matcher(lineString);
					if (nameMatcher.find())
					{
						//System.out.println(lineString);
						String newString = lineString.substring(18);
						engineer = new Engineer(newString);
						if (!set.contains(engineer))
						{
							set.add(engineer);
						}
						else 
						{
							for (Iterator iterator = set.iterator(); iterator.hasNext();)
							{
								Engineer engineer2 = (Engineer) iterator.next();
								if (engineer2.equals(engineer))
								{
									engineer = engineer2;
								}
							}
						}
						
					}
					while (defectMatcher.find())
					{
						//System.out.println(defectMatcher.group());
						//int index = defectMatcher.start(0);
						engineer.addDefect(defectMatcher.group());
					}
					
				}
				reader.close();
				
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		for (Iterator iterator = set.iterator(); iterator.hasNext();)
		{
			Engineer engineerInfo = (Engineer) iterator.next();
			engineerInfo.printEngineer();
			//System.out.println(engineerInfo.getNameString());
		}
		System.out.println("共"+set.size()+"人");

	}

}
