package com.holgerhees.indoorpos.persistance.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.persistance.dao.AbstractBaseDAO;

@Component("beaconDAO")
public class BeaconDAO extends AbstractBaseDAO<BeaconDTO>
{
	protected Class<BeaconDTO> getMappedClass()
	{
		return BeaconDTO.class;
	}
	
	public BeaconDTO getBeaconById(Long beaconId)
	{
		return queryForObject( "SELECT * FROM beacon WHERE id = ?", new Object[]{beaconId} );
	}	

	public boolean delete( Long beaconId )
	{
		return update("DELETE FROM beacon WHERE id = ?", beaconId );
	}
}
