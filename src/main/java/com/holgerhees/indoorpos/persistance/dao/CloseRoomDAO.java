package com.holgerhees.indoorpos.persistance.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.holgerhees.indoorpos.persistance.dto.CloseRoomDTO;
import com.holgerhees.indoorpos.persistance.dto.RoomDTO;
import com.holgerhees.shared.persistance.dao.AbstractBaseDAO;

@Component( "closeRoomDAO" )
public class CloseRoomDAO extends AbstractBaseDAO<CloseRoomDTO>
{
    protected Class<CloseRoomDTO> getMappedClass()
    {
        return CloseRoomDTO.class;
    }

    public boolean truncate()
    {
        update( "DELETE FROM close_room" );
        update( "ALTER TABLE close_room AUTO_INCREMENT = 1" );
        return true;
    }

	public List<CloseRoomDTO> getCloseRooms()
	{
		return query( "SELECT * FROM close_room" );
	}
}
