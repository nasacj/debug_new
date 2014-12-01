/**
 * 
 */
package com.ibm.parsertools.txtparser;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;



/**
 * @author Jeff
 *
 */
public class Engineer implements Comparable<Engineer>
{
	public Engineer()
	{
		defectsSet = new HashSet<String>();
	}
	
	public Engineer(String name)
	{
		nameString = name;
		defectsSet = new HashSet<String>();
	}
	
	public void addDefect(String defect)
	{
		this.defectsSet.add(defect);
	}
	
	private String nameString = null;

	private Set<String> defectsSet = null;

	public String getNameString()
	{
		return nameString;
	}

	public void setNameString(String nameString)
	{
		this.nameString = nameString;
	}
/*
	public List<String> getDefectsList()
	{
		return defectsList;
	}

	public void setDefectsList(List<String> defectsList)
	{
		this.defectsList = defectsList;
	}
*/	
	public void printEngineer()
	{
		System.out.println(this.getNameString());
		System.out.println("----------");
		for (Iterator iterator = defectsSet.iterator(); iterator.hasNext();)
		{
			String defect = (String) iterator.next();
			System.out.println(defect);
		}
		System.out.println();
		
	}

	@Override
	public int compareTo(Engineer o)
	{
		return this.nameString.compareTo(o.getNameString());
	}
	
	public boolean equals(Object anObject)
	{
		return this.nameString.equals(((Engineer)anObject).getNameString());
	}
	
	public int hashCode()
	{
		return this.nameString.hashCode();
	}
}
