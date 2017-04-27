package com.holgerhees.indoorpos.persistance.dao;

import org.springframework.stereotype.Component;

import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.persistance.dao.AbstractBaseDAO;

@Component("trackerDAO")
public class TrackerDAO extends AbstractBaseDAO<TrackerDTO>
{
	protected Class<TrackerDTO> getMappedClass()
	{
		return TrackerDTO.class;
	}
	
	public TrackerDTO getTrackerById(Long trackerId)
	{
		return queryForObject( "SELECT * FROM tracker WHERE id = ?", new Object[]{trackerId} );
	}	

	public boolean delete( Long trackerId )
	{
		return update("DELETE FROM tracker WHERE id = ?", trackerId );
	}
}
