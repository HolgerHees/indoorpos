package com.holgerhees.shared.persistance.dto;

import com.holgerhees.shared.persistance.annotations.DbColumn;

import java.util.Date;

public abstract class AbstractBaseDTO
{

    @DbColumn( name = "created",
            type = "datetime",
            updatable = false )
    private Date created;

    @DbColumn( name = "lastModified",
            type = "datetime" )
    private Date lastModified;

    public Date getCreated()
    {
        return created;
    }

    public void setCreated(Date created)
    {
        this.created = created;
    }

    public Date getLastModified()
    {
        return lastModified;
    }

    public void setLastModified(Date lastModified)
    {
        this.lastModified = lastModified;
    }
}
