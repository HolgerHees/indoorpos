package com.holgerhees.indoorpos.persistance.dto;

import com.holgerhees.shared.persistance.annotations.DbColumn;
import com.holgerhees.shared.persistance.annotations.DbTable;
import com.holgerhees.shared.persistance.dto.AbstractKeyDTO;

@DbTable( name = "beacon" )
public class BeaconDTO extends AbstractKeyDTO
{
    @DbColumn( name = "uuid",
               type = "varchar(255)" )
    private String uuid;

    @DbColumn( name = "name",
               type = "varchar(255)" )
    private String name;

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid( String uuid )
    {
        this.uuid = uuid;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }
}
