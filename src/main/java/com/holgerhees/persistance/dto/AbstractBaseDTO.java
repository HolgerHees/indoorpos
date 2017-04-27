package com.holgerhees.persistance.dto;

import java.util.Date;

import com.holgerhees.persistance.annotations.DbColumn;

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
