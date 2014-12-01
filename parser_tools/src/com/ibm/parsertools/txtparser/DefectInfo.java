package com.ibm.parsertools.txtparser;

import java.io.IOException;
import java.io.OutputStream;

public class DefectInfo implements Comparable<DefectInfo>
{
	private String UID;
	
	private String headline;
	
	private String owner;
	
	private String ownerEmail;
	
	private String closeDate;
	
	private String componentName;
	
	private String notesLog;

	public String getUID()
	{
		return UID;
	}

	public void setUID(String uid)
	{
		UID = uid;
	}

	public String getHeadline()
	{
		return headline;
	}

	public void setHeadline(String headline)
	{
		this.headline = headline;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public String getOwnerEmail()
	{
		return ownerEmail;
	}

	public void setOwnerEmail(String ownerEmail)
	{
		this.ownerEmail = ownerEmail;
	}

	public String getCloseDate()
	{
		return closeDate;
	}

	public void setCloseDate(String closeDate)
	{
		this.closeDate = closeDate;
	}

	public String getComponentName()
	{
		return componentName;
	}

	public void setComponentName(String componentName)
	{
		this.componentName = componentName;
	}

	public String getNotesLog()
	{
		return notesLog;
	}

	public void setNotesLog(String notesLog)
	{
		this.notesLog = notesLog;
	}
	
	public boolean equals(Object anObject)
	{
		if (UID instanceof String)
		{
			return this.UID.equals(anObject);
		}
		return this.UID.equals(((DefectInfo)anObject).getUID());
	}
	
	public int hashCode()
	{
		return this.UID.hashCode();
	}

	@Override
	public int compareTo(DefectInfo o)
	{
		return this.UID.compareTo(o.getUID());
	}
	
	public void printInfo(OutputStream outputstream) throws IOException
	{
		outputstream.write(("\""+this.UID+"\",").getBytes());
//		System.out.print("\t\t\t");
		outputstream.write(("\""+this.headline+"\",").getBytes());
//		System.out.print("\t\t\t");
		outputstream.write(("\""+this.owner+"\",").getBytes());
//		System.out.print("\t\t\t");
		outputstream.write(("\""+this.ownerEmail+"\",").getBytes());
//		System.out.print("\t\t\t");
		outputstream.write(("\""+this.closeDate+"\",").getBytes());
//		System.out.print("\t\t\t");
		outputstream.write(("\""+this.componentName+"\",").getBytes());
//		System.out.print("\t\t\t");
		outputstream.write(("\""+this.notesLog+"\"").getBytes());
		outputstream.write(("\r\n").getBytes());
		outputstream.flush();
	}
}
