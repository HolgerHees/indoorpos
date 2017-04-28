package com.holgerhees.indoorpos.persistance.dto;

import com.holgerhees.shared.persistance.annotations.DbColumn;
import com.holgerhees.shared.persistance.annotations.DbForeignKey;
import com.holgerhees.shared.persistance.annotations.DbTable;
import com.holgerhees.shared.persistance.dto.AbstractKeyDTO;

@DbTable( name = "area" )
public class AreaDTO extends AbstractKeyDTO
{
    @DbColumn( name = "room_id",
            type = "int(11)",
            updatable = false,
            foreignKey = {@DbForeignKey( target = RoomDTO.class,
                    field = "id",
                    onUpdate = "CASCADE",
                    onDelete = "CASCADE" )} )
    private Long roomId;

    @DbColumn( name = "top_left_x",
            type = "smallint(8)" )
    private int topLeftX;

    @DbColumn( name = "top_left_y",
            type = "smallint(8)" )
    private int topLeftY;

    @DbColumn( name = "bottom_right_x",
            type = "smallint(8)" )
    private int bottomRightX;

    @DbColumn( name = "bottom_right_y",
            type = "smallint(8)" )
    private int bottomRightY;

    public Long getRoomId()
    {
        return roomId;
    }

    public void setRoomId(Long roomId)
    {
        this.roomId = roomId;
    }

    public int getTopLeftX()
    {
        return topLeftX;
    }

    public void setTopLeftX(int topLeftX)
    {
        this.topLeftX = topLeftX;
    }

    public int getTopLeftY()
    {
        return topLeftY;
    }

    public void setTopLeftY(int topLeftY)
    {
        this.topLeftY = topLeftY;
    }

    public int getBottomRightX()
    {
        return bottomRightX;
    }

    public void setBottomRightX(int bottomRightX)
    {
        this.bottomRightX = bottomRightX;
    }

    public int getBottomRightY()
    {
        return bottomRightY;
    }

    public void setBottomRightY(int bottomRightY)
    {
        this.bottomRightY = bottomRightY;
    }
}
