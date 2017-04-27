package com.holgerhees.indoorpos.persistance.dto;

import com.holgerhees.persistance.annotations.DbColumn;
import com.holgerhees.persistance.annotations.DbForeignKey;
import com.holgerhees.persistance.annotations.DbTable;
import com.holgerhees.persistance.dto.AbstractKeyDTO;

@DbTable(name="tracked_beacon")
public class TrackedBeaconDTO extends AbstractKeyDTO
{
	@DbColumn( name="tracker_id", type="int(11)", updatable=false, foreignKey = { @DbForeignKey(target = TrackerDTO.class, field = "id", onUpdate = "CASCADE", onDelete = "CASCADE") } )
	private Long trackerId;

	@DbColumn( name="beacon_id", type="int(11)", updatable=false, foreignKey = { @DbForeignKey(target = BeaconDTO.class, field = "id", onUpdate = "CASCADE", onDelete = "CASCADE") } )
	private Long beaconId;

	@DbColumn( name="power", type="tinyint(2)" )
	private int power;

	public Long getTrackerId()
	{
		return trackerId;
	}

	public void setTrackerId(Long trackerId)
	{
		this.trackerId = trackerId;
	}

	public Long getBeaconId()
	{
		return beaconId;
	}

	public void setBeaconId(Long beaconId)
	{
		this.beaconId = beaconId;
	}

	public int getPower()
	{
		return power;
	}

	public void setPower(int power)
	{
		this.power = power;
	}
}
