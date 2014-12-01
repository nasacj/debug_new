/**
 * 
 */
package com.ibm.parsertools.memoryleak;


/**
 * @author qianchj
 *
 */
public class MemoryInfo
{
	private String callerAddress;
	private String address;
	private String stringLine;
	int size;
	
	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public boolean equals(Object anObject)
	{
		return this.address.equals(((MemoryInfo)anObject).getAddress()) && this.callerAddress.equals(((MemoryInfo)anObject).getCallerAddress());
	}
	
	public int hashCode()
	{
		return this.address.hashCode() + this.callerAddress.hashCode();
	}
	
	public String getStringLine()
	{
		return stringLine;
	}
	public void setStringLine(String stringLine)
	{
		this.stringLine = stringLine;
	}
	public String getCallerAddress()
	{
		return callerAddress;
	}
	public void setCallerAddress(String callerAddress)
	{
		this.callerAddress = callerAddress;
	}
	public String getAddress()
	{
		return address;
	}
	public void setAddress(String address)
	{
		this.address = address;
	}
	
}
