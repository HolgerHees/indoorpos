package com.holgerhees.indoorpos.persistance.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.shared.persistance.dao.AbstractBaseDAO;

@Component( "beaconDAO" )
public class BeaconDAO extends AbstractBaseDAO<BeaconDTO>
{
	protected Class<BeaconDTO> getMappedClass()
	{
		return BeaconDTO.class;
	}

	public BeaconDTO getBeaconById(Long beaconId)
	{
		return queryForObject("SELECT * FROM beacon WHERE id = ?", new Object[] { beaconId });
	}

	public BeaconDTO getBeaconByUUID(String uuid)
	{
		return queryForObject("SELECT * FROM beacon WHERE uuid = ?", new Object[] { uuid });
	}

	public boolean delete(Long beaconId)
	{
		return update("DELETE FROM beacon WHERE id = ?", beaconId);
	}

	public boolean truncate()
	{
		update("DELETE FROM beacon");
		update("ALTER TABLE beacon AUTO_INCREMENT = 1");
		return true;
	}

	public Map<String, BeaconDTO> getBeaconUUIDMap()
	{
		List<BeaconDTO> beacons = query("SELECT * FROM beacon");

		Map<String, BeaconDTO> beaconMap = new HashMap<>();
		for( BeaconDTO beaconDTO : beacons )
		{
			beaconMap.put(beaconDTO.getUuid(), beaconDTO);
		}

		return beaconMap;
	}

	public Map<Long, BeaconDTO> getBeaconIDMap()
	{
		List<BeaconDTO> beacons = query("SELECT * FROM beacon");

		Map<Long, BeaconDTO> beaconMap = new HashMap<>();
		for( BeaconDTO beaconDTO : beacons )
		{
			beaconMap.put(beaconDTO.getId(), beaconDTO);
		}

		return beaconMap;
	}
}
