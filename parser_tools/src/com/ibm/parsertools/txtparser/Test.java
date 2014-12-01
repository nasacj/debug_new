package com.ibm.parsertools.txtparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String lineString = "0x2ab98a2c ===> Memory Stay ------> 0x106080a8 (size 100, <Unknown>:0   )";
		int size = Integer.parseInt(lineString.substring(lineString.indexOf("size")+4, lineString.indexOf(",")).trim());
		System.out.println(size);
		Pattern namePattern = Pattern.compile("0x[A-F|a-f|0-9]{8}");
		Matcher nameMatcher = namePattern.matcher(lineString);
		if (nameMatcher.find(0))
			System.out.println(nameMatcher.group(0));
		if (nameMatcher.find(1))
			System.out.println(nameMatcher.group());


	}

}
