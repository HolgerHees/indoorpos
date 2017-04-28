package com.holgerhees.indoorpos.persistance.dto;

import com.holgerhees.shared.persistance.annotations.DbColumn;
import com.holgerhees.shared.persistance.annotations.DbTable;
import com.holgerhees.shared.persistance.dto.AbstractKeyDTO;

@DbTable( name = "tracker" )
public class TrackerDTO extends AbstractKeyDTO
{
	@DbColumn( name = "pos_x",
	           type = "smallint(8)" )
	private int posX;

	@DbColumn( name = "pos_y",
	           type = "smallint(8)" )
	private int posY;

	@DbColumn( name = "name",
	           type = "varchar(255)" )
	private String name;

	@DbColumn( name = "room",
	           type = "varchar(255)" )
	private String room;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getPosX()
	{
		return posX;
	}

	public void setPosX(int posX)
	{
		this.posX = posX;
	}

	public int getPosY()
	{
		return posY;
	}

	public void setPosY(int posY)
	{
		this.posY = posY;
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
