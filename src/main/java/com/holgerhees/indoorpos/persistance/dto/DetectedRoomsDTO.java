package com.holgerhees.indoorpos.persistance.dto;

import com.holgerhees.shared.persistance.annotations.DbColumn;
import com.holgerhees.shared.persistance.annotations.DbForeignKey;
import com.holgerhees.shared.persistance.annotations.DbIndex;
import com.holgerhees.shared.persistance.annotations.DbTable;
import com.holgerhees.shared.persistance.dto.AbstractBaseDTO;
import com.holgerhees.shared.persistance.dto.AbstractKeyDTO;

@DbTable( name = "detected_rooms" )
public class DetectedRoomsDTO extends AbstractKeyDTO
{
    @DbColumn( name = "room_id",
               type = "int(11)",
               updatable = false,
               foreignKey = { @DbForeignKey( target = RoomDTO.class,
                                             field = "id",
                                             onUpdate = "CASCADE",
                                             onDelete = "CASCADE" ) } )
    @DbIndex( type = DbIndex.Type.UNIQUE, group = "primary" )
    private Long roomId;

    @DbColumn( name = "beacon_id",
               type = "int(11)",
               updatable = false,
               foreignKey = { @DbForeignKey( target = BeaconDTO.class,
                                             field = "id",
                                             onUpdate = "CASCADE",
                                             onDelete = "CASCADE" ) } )
    @DbIndex( type = DbIndex.Type.UNIQUE, group = "primary" )
    private Long beaconId;

    @DbColumn( name = "distance",
               type = "int(11)" )
    private int distance;

    public Long getRoomId()
    {
        return roomId;
    }

    public void setRoomId( Long roomId )
    {
        this.roomId = roomId;
    }

    public Long getBeaconId()
    {
        return beaconId;
    }

    public void setBeaconId( Long beaconId )
    {
        this.beaconId = beaconId;
    }

    public int getDistance()
    {
        return distance;
    }

    public void setDistance( int distance )
    {
        this.distance = distance;
    }
}
