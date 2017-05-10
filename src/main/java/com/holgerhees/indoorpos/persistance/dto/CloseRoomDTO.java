package com.holgerhees.indoorpos.persistance.dto;

import com.holgerhees.shared.persistance.annotations.DbColumn;
import com.holgerhees.shared.persistance.annotations.DbForeignKey;
import com.holgerhees.shared.persistance.annotations.DbTable;
import com.holgerhees.shared.persistance.dto.AbstractKeyDTO;

@DbTable( name = "close_room" )
public class CloseRoomDTO extends AbstractKeyDTO
{
	@DbColumn( name = "room_id",
	           type = "int(11)",
	           updatable = false,
	           foreignKey = { @DbForeignKey( target = RoomDTO.class,
	                                         field = "id",
	                                         onUpdate = "CASCADE",
	                                         onDelete = "CASCADE" ) } )
	private Long roomId;

	@DbColumn( name = "close_room_id",
	           type = "int(11)",
	           updatable = false,
	           foreignKey = { @DbForeignKey( target = RoomDTO.class,
	                                         field = "id",
	                                         onUpdate = "CASCADE",
	                                         onDelete = "CASCADE" ) } )
	private Long closeRoomId;

	public Long getRoomId()
	{
		return roomId;
	}

	public void setRoomId(Long roomId)
	{
		this.roomId = roomId;
	}

	public Long getCloseRoomId()
	{
		return closeRoomId;
	}

	public void setCloseRoomId(Long closeRoomId)
	{
		this.closeRoomId = closeRoomId;
	}
}
