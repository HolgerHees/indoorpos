package com.holgerhees.indoorpos.persistance.dao;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;
import com.holgerhees.persistance.dao.AbstractBaseDAO;

@Component( "trackedBeaconDAO" )
public class TrackedBeaconDAO extends AbstractBaseDAO<TrackedBeaconDTO>
{
	protected Class<TrackedBeaconDTO> getMappedClass()
	{
		return TrackedBeaconDTO.class;
	}

	public TrackedBeaconDTO getTrackedBeaconById(Long trackedBeaconId)
	{
		return queryForObject("SELECT * FROM tracked_beacon WHERE id = ?", new Object[] { trackedBeaconId });
	}

	public boolean delete(Long trackedBeaconId)
	{
		return update("DELETE FROM tracked_beacon WHERE id = ?", trackedBeaconId);
	}

	public List<TrackedBeaconDTO> getTrackedBeacons()
	{
		Date date = new Date();
		date.setTime(date.getTime() - 1000 * 2);

		return query("SELECT * FROM tracked_beacon ORDER BY created DESC");
	}
}
