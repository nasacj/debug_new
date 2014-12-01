package com.ibm.parsertools.memoryleak;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ParserAction
{	
	private HashSet<MemoryInfo> record_1 = new HashSet<MemoryInfo>();
	//private HashSet<MemoryInfo> record_2 = new HashSet<MemoryInfo>();
	private HashMap<String, HashSet<MemoryInfo>> table = new HashMap<String, HashSet<MemoryInfo>>();
	private int totalsize = 0;
	
	private void printTable()
	{
		System.out.println("Caller\t\tTimes\tTotal Bytes");
		for (Map.Entry<String, HashSet<MemoryInfo>> m : table.entrySet()) 
		{
			String caller = m.getKey();
			HashSet<MemoryInfo> recordinfo = m.getValue();
			System.out.print(caller + "\t");
			System.out.print(recordinfo.size() + "\t");
			int size = 0;
			for (MemoryInfo info : recordinfo)
			{
				size += info.getSize();
			}
			System.out.println(size);
		}
	}
	
	public HashMap<String, HashSet<MemoryInfo>> parse(File inputFile)
	{
		HashSet<MemoryInfo> set = null;
		File file = inputFile;
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
			Pattern namePattern = Pattern.compile("0x[A-F|a-f|0-9]{6}");
			Pattern recordPattern = Pattern.compile("[0-9]{4}/[0-9]{2}/[0-9]{2}");
			
			while ((lineString = reader.readLine()) != null)
			{
				Matcher nameMatcher = namePattern.matcher(lineString);
				if (nameMatcher.find(0))
				{
					String caller = null;
					//caller = nameMatcher.group(0);
					caller = lineString.substring(nameMatcher.start(), nameMatcher.end()+ 2).trim();
					
					MemoryInfo info = new MemoryInfo();
					info.setCallerAddress(caller);
					nameMatcher.find(1);
					String address = nameMatcher.group();
					info.setAddress(address);
					info.setStringLine(lineString);
					int size = Integer.parseInt(lineString.substring(lineString.indexOf("size")+4, lineString.indexOf(",")).trim());
					info.setSize(size);
					record_1.add(info);
					
					totalsize += size;
					
					if(table.containsKey(caller))
						table.get(caller).add(info);
					else
					{
						set = new HashSet<MemoryInfo>();
						set.add(info);
						table.put(caller, set);
					}
					
					continue;
				}
				
			}
			reader.close();
			
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		System.out.println(table.size() + " Callers stay! Totally: " + totalsize + " Bytes");
		System.out.println("----------------------------------");
		
		this.printTable();
		
		
		return table;
	}
}
