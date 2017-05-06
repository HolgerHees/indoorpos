package com.holgerhees.indoorpos.persistance.dao;

import com.holgerhees.indoorpos.persistance.dto.RoomDTO;
import com.holgerhees.shared.persistance.dao.AbstractBaseDAO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component( "roomDAO" )
public class RoomDAO extends AbstractBaseDAO<RoomDTO>
{
    protected Class<RoomDTO> getMappedClass()
    {
        return RoomDTO.class;
    }

    public boolean truncate()
    {
        update( "DELETE FROM room" );
        update( "ALTER TABLE room AUTO_INCREMENT = 1" );
        return true;
    }

    public List<RoomDTO> getRooms()
    {
        return query( "SELECT * FROM room" );
    }
}
