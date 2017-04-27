package com.holgerhees.indoorpos.persistance.dto;

import com.holgerhees.persistance.annotations.DbColumn;
import com.holgerhees.persistance.annotations.DbTable;
import com.holgerhees.persistance.dto.AbstractKeyDTO;

@DbTable(name = "beacon")
public class BeaconDTO extends AbstractKeyDTO
{
	@DbColumn(name = "uuid",
	          type = "varchar(255)")
	private String uuid;

	@DbColumn(name = "name",
	          type = "varchar(255)")
	private String name;

	@DbColumn(name = "room",
	          type = "varchar(255)",
	          nullable = true )
	private String room;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getRoom()
	{
		return room;
	}

	public void setRoom(String room)
	{
		this.room = room;
	}
}
