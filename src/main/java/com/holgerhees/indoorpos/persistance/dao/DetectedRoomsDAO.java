package com.holgerhees.indoorpos.persistance.dao;

import com.holgerhees.indoorpos.persistance.dto.DetectedRoomsDTO;
import com.holgerhees.indoorpos.persistance.dto.RoomDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;
import com.holgerhees.shared.persistance.dao.AbstractBaseDAO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component( "detectedRoomsDAO" )
public class DetectedRoomsDAO extends AbstractBaseDAO<DetectedRoomsDTO>
{
    protected Class<DetectedRoomsDTO> getMappedClass()
    {
        return DetectedRoomsDTO.class;
    }

    public DetectedRoomsDTO getTrackedBeaconById( Long trackedBeaconId )
    {
        return queryForObject( "SELECT * FROM detected_rooms WHERE id = ?", new Object[]{ trackedBeaconId } );
    }

    public boolean delete( Long detectedRoomId )
    {
        return update( "DELETE FROM detected_rooms WHERE id = ?", detectedRoomId );
    }

    public List<Long> getDetectedRooms()
    {
        return query( "SELECT room_id FROM detected_rooms GROUP BY room_id", null, new RowMapper<Long>()
        {
            @Override
            public Long mapRow( ResultSet resultSet, int i ) throws SQLException
            {
                return resultSet.getLong( "room_id" );
            }
        });
    }

    public Map<Long, List<DetectedRoomsDTO>> getDetectedRoomsByBeacon()
    {
        List<DetectedRoomsDTO> detectedRoomsDTOs = query( "SELECT * FROM detected_rooms" );

        Map<Long, List<DetectedRoomsDTO>> detectedRoomsMap = new HashMap<>();
        for( DetectedRoomsDTO detectedRoomsDTO : detectedRoomsDTOs )
        {
            Long beaconId = detectedRoomsDTO.getBeaconId();

            List<DetectedRoomsDTO> roomsDTOs = detectedRoomsMap.get( beaconId );
            if( roomsDTOs == null )
            {
                roomsDTOs = new ArrayList<>();
                detectedRoomsMap.put( beaconId, roomsDTOs );
            }

            roomsDTOs.add( detectedRoomsDTO );
        }

        return detectedRoomsMap;
    }
}
