/**
 * 
 */
package com.ibm.parsertools.txtparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


/**
 * @author NASa
 * 
 */
public class DefectFilter
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Map<String, DefectInfo> map = new HashMap<String, DefectInfo>();
//		HashSet<DefectInfo> defectSet = new HashSet<DefectInfo>();
		HashSet<String> needDefect = new HashSet<String>();
		SAXReader saxReader = new SAXReader();
		Document document = null;
		try
		{
			document = saxReader.read(new File(args[0]));
			
		}
		catch (DocumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Element rootElement = document.getRootElement();
		Element worksheetElement = rootElement.element("Worksheet");
		Element tableElement = worksheetElement.element("Table");
		for (Iterator i = tableElement.elementIterator("Row"); i.hasNext();)
		{
			Element row = (Element) i.next();
			DefectInfo defectInfo = new DefectInfo();
			int count = 0;
			for (Iterator iterator = row.elementIterator("Cell"); iterator.hasNext(); count++)
			{
				Element cell = (Element) iterator.next();
				Element data = cell.element("Data");
				switch (count)
				{
					case 1:
						defectInfo.setUID(data.getTextTrim());
						break;
					case 2:
						defectInfo.setHeadline(data.getText());
						break;
					case 3:
						defectInfo.setOwner(data.getText());
						break;
					case 4:
						defectInfo.setOwnerEmail(data.getText());
						break;
					case 5:
						defectInfo.setCloseDate(data.getText());
						break;
					case 7:
						defectInfo.setComponentName(data.getText());
						break;
					case 8:
						defectInfo.setNotesLog(data.getText());
						break;
					default:
						break;
				}
				//System.out.print(data.getText()+ ";");
			}
//			defectSet.add(defectInfo);
			map.put(defectInfo.getUID(), defectInfo);
			//System.out.println("\n-----------------------\n");
//			if ("PC416246".equals(defectInfo.getUID()))
//			{
//				System.out.println(defectInfo.getUID());
//			}
			
		}
		
		File file = new File(args[1]);
		BufferedReader reader = null;
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
			Pattern defectPattern = Pattern.compile("[A-Z|a-z][A-Z|a-z][0-9]{6}");
			
			while ((lineString = reader.readLine()) != null)
			{
				Matcher defectMatcher = defectPattern.matcher(lineString);
				while (defectMatcher.find())
				{
					//System.out.println(defectMatcher.group());
					//int index = defectMatcher.start(0);
					needDefect.add(defectMatcher.group());
					//System.out.println(defectMatcher.group());
				}
			}
			reader.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		for (Iterator iterator = defectSet.iterator(); iterator.hasNext();)
//		{
//			DefectInfo defect = (DefectInfo) iterator.next();
//			//System.out.print(defect);
//			//System.out.println(new String("hello").hashCode() == new String("hello").hashCode());
//			if (needDefect.contains(defect))
//			{
//				System.out.print(" YES");
//			}
//			//System.out.println();
//		}
		
//		System.out.println(needDefect.contains("XB055491"));
		OutputStream out = null;
		try
		{
			out = new FileOutputStream(new File("Defects.csv"));
		} catch (FileNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try
		{
			out.write(("\"UID\",").getBytes());
	//		System.out.print("\t\t\t");
			out.write(("\"HeadLine\",").getBytes());
	//		System.out.print("\t\t\t");
			out.write(("\"Owner\",").getBytes());
	//		System.out.print("\t\t\t");
			out.write(("\"OwnerEmail\",").getBytes());
	//		System.out.print("\t\t\t");
			out.write(("\"CloseDate\",").getBytes());
	//		System.out.print("\t\t\t");
			out.write(("\"Component\",").getBytes());
	//		System.out.print("\t\t\t");
			out.write(("\"NotLog\"").getBytes());
			out.write(("\r\n").getBytes());
			out.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		for (Iterator iterator = needDefect.iterator(); iterator.hasNext();)
		{
			String string = (String) iterator.next();
			if (!map.containsKey(string))
			{
				System.out.println(string+" Not Found");
			}
			else
			{
				DefectInfo defectInfo = (DefectInfo)map.get(string);
				try
				{
					defectInfo.printInfo(out);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
//		System.out.println(defectSet.contains("XB055491"));
//		System.out.println(needDefect.contains("XB055491"));
		
//		for (String defect : map.keySet())
//		{
//			DefectInfo defectInfo = map.get(defect);
//			
//		}
	}

}
