package com.holgerhees.persistance.schema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Table
{
	private Class<?> dtoClass;
	private String name;
	private Map<String, Column> columns = new LinkedHashMap<String, Column>();
	private Map<String, Index> indexes = new LinkedHashMap<String, Index>();
	private Column[] primaryColumns;

	public Table(Class<?> dtoClass, String name)
	{
		this.dtoClass = dtoClass;
		this.name = name;
	}

	public Class<?> getDtoClass()
	{
		return dtoClass;
	}

	public String getName()
	{
		return name;
	}

	public List<Column> getColumns()
	{
		return new ArrayList<Column>(columns.values());
	}

	public Column getColumn(String name)
	{
		return columns.get(name);
	}

	public void addColumn(Column column)
	{
		columns.put(column.getName(), column);
	}

	public List<Index> getIndexes()
	{
		return new ArrayList<Index>(indexes.values());
	}

	public void addIndex(Index index)
	{
		indexes.put(index.getName(), index);
	}

	public Column[] getPrimaryColumns()
	{
		return primaryColumns;
	}

	public void setPrimaryColumns(Column[] columns)
	{
		primaryColumns = columns;
	}
}
