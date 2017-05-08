package com.holgerhees.indoorpos.frontend.service;

import com.holgerhees.indoorpos.persistance.dao.AreaDAO;
import com.holgerhees.indoorpos.persistance.dao.BeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.RoomDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.AreaDTO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.RoomDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component( "daoCacheService" )
public class DAOCacheService
{
    @Autowired
    AreaDAO areaDAO;

    @Autowired
    BeaconDAO beaconDAO;

    @Autowired
    RoomDAO roomDAO;

    @Autowired
    TrackerDAO trackerDAO;

    List<AreaDTO> areaCache;
    List<BeaconDTO> beaconCache;
    Map<String, BeaconDTO> beaconUuidMapCache;
    Map<Long, BeaconDTO> beaconIdMapCache;
    Map<Long, RoomDTO> roomIdMapCache;
    List<TrackerDTO> trackerCache;
    Map<String, TrackerDTO> trackerUuidMapCache;
    Map<Long, TrackerDTO> trackerIdMapCache;

    @PostConstruct
    public void init()
    {
        areaCache = areaDAO.getAreas();
        beaconCache = beaconDAO.getBeacons();
        beaconUuidMapCache = new HashMap<>();
        for( BeaconDTO beaconDTO : beaconCache )
        {
            beaconUuidMapCache.put( beaconDTO.getUuid(), beaconDTO );
        }
        beaconIdMapCache = new HashMap<>();
        for( BeaconDTO beaconDTO : beaconCache )
        {
            beaconIdMapCache.put( beaconDTO.getId(), beaconDTO );
        }
        List<RoomDTO> rooms = roomDAO.getRooms();
        roomIdMapCache = new HashMap<>();
        for( RoomDTO room : rooms )
        {
            roomIdMapCache.put( room.getId(), room );
        }
        trackerCache = trackerDAO.getTracker();
        trackerUuidMapCache = new HashMap<>();
        for( TrackerDTO trackerDTO : trackerCache )
        {
            trackerUuidMapCache.put( trackerDTO.getUuid(), trackerDTO );
        }
        trackerIdMapCache = new HashMap<>();
        for( TrackerDTO trackerDTO : trackerCache )
        {
            trackerIdMapCache.put( trackerDTO.getId(), trackerDTO );
        }
    }

    public List<AreaDTO> getAreas()
    {
        return areaCache;
    }

    public List<BeaconDTO> getBeacons()
    {
        return beaconCache;
    }

    public BeaconDTO getBeaconByUUID( String uuid )
    {
        return beaconUuidMapCache.get( uuid );
    }

    public BeaconDTO getBeaconById( Long id )
    {
        return beaconIdMapCache.get( id );
    }


    public RoomDTO getRoomById( Long id )
    {
        return roomIdMapCache.get( id );
    }

    public List<TrackerDTO> getTracker()
    {
        return trackerCache;
    }

    public TrackerDTO getTrackerByUUID( String uuid )
    {
        return trackerUuidMapCache.get( uuid );
    }

    public TrackerDTO getTrackerById( Long id )
    {
        return trackerIdMapCache.get( id );
    }

	public TrackerDTO getTrackerByIp( String ip )
	{
		for( TrackerDTO trackerDTO: trackerCache )
		{
			if( trackerDTO.getIp().equals( ip ) )
			{
				return trackerDTO;
			}
		}

		return null;
	}
}
