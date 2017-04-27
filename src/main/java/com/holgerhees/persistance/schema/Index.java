package com.holgerhees.persistance.schema;

public class Index
{
	private String name;
	private String definition;
	
	public Index( String name, String definition )
	{
		this.name = name;
		this.definition = definition;
	}
	
	public String getName()
	{
		return name;
	}
	public String getDefinition()
	{
		return definition;
	}
}
