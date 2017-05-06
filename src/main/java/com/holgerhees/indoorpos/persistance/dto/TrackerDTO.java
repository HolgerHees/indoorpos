package com.holgerhees.indoorpos.persistance.dto;

import com.holgerhees.shared.persistance.annotations.DbColumn;
import com.holgerhees.shared.persistance.annotations.DbForeignKey;
import com.holgerhees.shared.persistance.annotations.DbIndex;
import com.holgerhees.shared.persistance.annotations.DbTable;
import com.holgerhees.shared.persistance.dto.AbstractKeyDTO;

@DbTable( name = "tracker" )
public class TrackerDTO extends AbstractKeyDTO
{
    @DbColumn( name = "uuid",
               type = "varchar(255)" )
    private String uuid;

    @DbColumn( name = "room_id",
               type = "int(11)",
               updatable = false,
               foreignKey = { @DbForeignKey( target = RoomDTO.class,
                                             field = "id",
                                             onUpdate = "CASCADE",
                                             onDelete = "CASCADE" ) } )
    private Long roomId;

    @DbColumn( name = "pos_x",
               type = "smallint(4)" )
    private int posX;

    @DbColumn( name = "pos_y",
               type = "smallint(4)" )
    private int posY;

    @DbColumn( name = "name",
               type = "varchar(255)" )
    private String name;

    @DbColumn( name = "ip",
               type = "varchar(255)" )
    @DbIndex( type = DbIndex.Type.INDEX)
    private String ip;

    @DbColumn( name = "rssi_offset",
               type = "tinyint(2)" )
    private int rssiOffset;

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid( String uuid )
    {
        this.uuid = uuid;
    }

    public Long getRoomId()
    {
        return roomId;
    }

    public void setRoomId( Long roomId )
    {
        this.roomId = roomId;
    }

    public int getPosX()
    {
        return posX;
    }

    public void setPosX( int posX )
    {
        this.posX = posX;
    }

    public int getPosY()
    {
        return posY;
    }

    public void setPosY( int posY )
    {
        this.posY = posY;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getIp()
    {
        return ip;
    }

    public void setIp( String ip )
    {
        this.ip = ip;
    }

    public int getRssiOffset()
    {
        return rssiOffset;
    }

    public void setRssiOffset( int rssiOffset )
    {
        this.rssiOffset = rssiOffset;
    }
}
