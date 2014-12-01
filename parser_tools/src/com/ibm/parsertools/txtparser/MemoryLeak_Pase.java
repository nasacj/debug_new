package com.ibm.parsertools.txtparser;

import java.io.File;

import com.ibm.parsertools.memoryleak.ParserAction;

public class MemoryLeak_Pase
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		if(args.length != 1)
		{
			System.out.println("USAGE: <filepath>");
			return;
		}
		ParserAction pa = new ParserAction();
		pa.parse(new File(args[0]));
	}

}
