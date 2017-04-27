package com.holgerhees.indoorpos.persistance.dto;

import com.holgerhees.persistance.annotations.DbColumn;
import com.holgerhees.persistance.annotations.DbForeignKey;
import com.holgerhees.persistance.annotations.DbTable;
import com.holgerhees.persistance.dto.AbstractKeyDTO;

@DbTable(name="beacon")
public class BeaconDTO extends AbstractKeyDTO
{
	@DbColumn( name="uuid", type="varchar(255)" )
	private String uuid;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}
}
