package com.holgerhees.indoorpos.persistance.dto;

import com.holgerhees.persistance.annotations.DbColumn;
import com.holgerhees.persistance.annotations.DbForeignKey;
import com.holgerhees.persistance.annotations.DbTable;
import com.holgerhees.persistance.dto.AbstractKeyDTO;

@DbTable(name="beacon")
public class BeaconDTO extends AbstractKeyDTO
{
	@DbColumn( name="tracker_id", type="int(11)", updatable=false, foreignKey = { @DbForeignKey(target = TrackerDTO.class, field = "id", onUpdate = "CASCADE", onDelete = "CASCADE") } )
	private Long trackerId;

	@DbColumn( name="uuid", type="varchar(255)" )
	private String uuid;

	@DbColumn( name="power", type="tinyint(2)" )
	private String power;

	public Long getTrackerId()
	{
		return trackerId;
	}

	public void setTrackerId(Long trackerId)
	{
		this.trackerId = trackerId;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getPower()
	{
		return power;
	}

	public void setPower(String power)
	{
		this.power = power;
	}
}
