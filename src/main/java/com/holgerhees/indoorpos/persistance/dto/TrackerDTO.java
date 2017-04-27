package com.holgerhees.indoorpos.persistance.dto;

import com.holgerhees.persistance.annotations.DbColumn;
import com.holgerhees.persistance.annotations.DbForeignKey;
import com.holgerhees.persistance.annotations.DbTable;
import com.holgerhees.persistance.dto.AbstractKeyDTO;

@DbTable(name="tracker")
public class TrackerDTO extends AbstractKeyDTO
{
	@DbColumn( name="name", type="varchar(255)" )
	private String name;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
