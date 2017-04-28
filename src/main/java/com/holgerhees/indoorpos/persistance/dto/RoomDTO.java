package com.holgerhees.indoorpos.persistance.dto;

import com.holgerhees.shared.persistance.annotations.DbColumn;
import com.holgerhees.shared.persistance.annotations.DbTable;
import com.holgerhees.shared.persistance.dto.AbstractKeyDTO;

@DbTable( name = "room" )
public class RoomDTO extends AbstractKeyDTO
{
	@DbColumn( name = "floor",
	           type = "smallint(8)" )
	private int floor;

	@DbColumn( name = "name",
	           type = "varchar(255)" )
	private String name;

	public int getFloor()
	{
		return floor;
	}

	public void setFloor(int floor)
	{
		this.floor = floor;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
