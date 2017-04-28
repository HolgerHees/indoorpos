package com.holgerhees.indoorpos.persistance.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.holgerhees.indoorpos.persistance.dto.RoomDTO;
import com.holgerhees.shared.persistance.dao.AbstractBaseDAO;

@Component( "roomDAO" )
public class RoomDAO extends AbstractBaseDAO<RoomDTO>
{
	protected Class<RoomDTO> getMappedClass()
	{
		return RoomDTO.class;
	}

	public RoomDTO getRoomById(Long roomId)
	{
		return queryForObject("SELECT * FROM room WHERE id = ?", new Object[] { roomId });
	}

	public boolean delete(Long roomId)
	{
		return update("DELETE FROM room WHERE id = ?", roomId);
	}

	public boolean truncate()
	{
		update("DELETE FROM room");
		update("ALTER TABLE room AUTO_INCREMENT = 1");
		return true;
	}

	public Map<Long, RoomDTO> getRoomIDMap()
	{
		List<RoomDTO> rooms = query("SELECT * FROM room");

		Map<Long, RoomDTO> roomMap = new HashMap<>();
		for( RoomDTO room : rooms )
		{
			roomMap.put(room.getId(), room);
		}

		return roomMap;
	}

	public List<RoomDTO> getRooms()
	{
		return query("SELECT * FROM room");
	}
}
