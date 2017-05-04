package com.holgerhees.indoorpos.frontend.websockets.overview;

/**
 * Created by hhees on 03.05.17.
 */

import com.holgerhees.indoorpos.frontend.service.CacheService;
import com.holgerhees.indoorpos.frontend.service.CacheWatcherClient;
import com.holgerhees.indoorpos.frontend.service.CacheWatcherService;
import com.holgerhees.indoorpos.frontend.websockets.EndPointWatcherClient;
import com.holgerhees.indoorpos.frontend.websockets.samples.SamplesEndPoint;
import com.holgerhees.indoorpos.persistance.dao.AreaDAO;
import com.holgerhees.indoorpos.persistance.dao.BeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.RoomDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.AreaDTO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.RoomDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.indoorpos.util.TrackingHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component( "overviewWatcher" )
public class OverviewWatcher implements CacheWatcherClient, EndPointWatcherClient
{
    private static Log LOGGER = LogFactory.getLog( OverviewWatcher.class );

    @Autowired
    AreaDAO areaDAO;

    @Autowired
    RoomDAO roomDAO;

    @Autowired
    BeaconDAO beaconDAO;

    @Autowired
    TrackerDAO trackerDAO;

    @Autowired
    CacheService cacheService;

    @Autowired
    CacheWatcherService cacheWatcherService;

    private List<Long> lastDetectedRooms;

	private class Area
    {
        String key;
        int topLeftX;
        int topLeftY;
        int bottomRightX;
        int bottomRightY;
        int floor;
    }

    private class Tracker
    {
        String key;
        String name;
        int floor;
        int posX;
        int posY;
    }

    @PostConstruct
    public void init()
    {
	    cacheWatcherService.addWatcher( this );
	    OverviewEndPoint.setWatcher( this);
    }

	@Override
	public void notifyCacheChange()
	{
		if( !OverviewEndPoint.hasSessions() )
		{
			return;
		}

		List<Long> detectedRooms = getDetectedRooms();
		if( detectedRooms.equals( lastDetectedRooms ) )
		{
			return;
		}

		lastDetectedRooms = detectedRooms;

		List<OverviewWatcher.Area> areas = getAreas( detectedRooms );
		OverviewEndPoint.broadcastMessage( "area", areas );
	}

	@Override
	public void notifyNewSession(Session userSession)
	{
		OverviewEndPoint.sendMessage( userSession,"tracker", getTracker() );

		List<Long> detectedRooms = getDetectedRooms();
		List<OverviewWatcher.Area> areas = getAreas( detectedRooms );
		OverviewEndPoint.broadcastMessage( "area", areas );
	}

	private List<Long> getDetectedRooms()
	{
		Map<Long, TrackerDTO> trackerDTOMap = trackerDAO.getTrackerIDMap();
		List<BeaconDTO> beaconDTOs = beaconDAO.getBeacons();

		List<Long> detectedRooms = new ArrayList<>();

		for( BeaconDTO beaconDTO : beaconDTOs )
		{
			List<CacheService.TrackedBeacon> trackedBeaconDTOs = cacheService.getTrackedBeacons( beaconDTO.getId() );
			CacheService.TrackedBeacon activeTracker = null;
			for( CacheService.TrackedBeacon trackedBeaconDTO : trackedBeaconDTOs )
			{
				if( activeTracker == null || TrackingHelper.compareTracker( activeTracker, trackedBeaconDTO ) > 0 )
				{
					activeTracker = trackedBeaconDTO;
				}
			}
			if( activeTracker != null )
			{
				TrackerDTO trackerDTO = trackerDTOMap.get( activeTracker.getTrackerId() );
				detectedRooms.add( trackerDTO.getRoomId() );
			}
		}
		return detectedRooms;
	}

    private List<OverviewWatcher.Area> getAreas( List<Long> detectedRooms )
    {
        Map<Long, RoomDTO> roomDTOMap = roomDAO.getRoomIDMap();

        List<AreaDTO> areas = areaDAO.getAreas();

        List<OverviewWatcher.Area> entries = new ArrayList<>();
        for( AreaDTO area : areas )
        {
            if( !detectedRooms.contains( area.getRoomId() ) )
            {
                continue;
            }

            OverviewWatcher.Area _area = new OverviewWatcher.Area();
            _area.key = "area" + area.getId();
            _area.topLeftX = area.getTopLeftX();
            _area.topLeftY = area.getTopLeftY();
            _area.bottomRightX = area.getBottomRightX();
            _area.bottomRightY = area.getBottomRightY();
            _area.floor = roomDTOMap.get( area.getRoomId() ).getFloor();

            entries.add( _area );
        }

        return entries;
    }

    private List<OverviewWatcher.Tracker> getTracker()
    {
        Map<Long, RoomDTO> roomDTOMap = roomDAO.getRoomIDMap();
        List<TrackerDTO> trackers = trackerDAO.getTracker();

        List<OverviewWatcher.Tracker> result = new ArrayList<>();
        for( TrackerDTO tracker : trackers )
        {
            OverviewWatcher.Tracker _tracker = new OverviewWatcher.Tracker();
            _tracker.key = "tracker" + tracker.getId();
            _tracker.name = tracker.getName();
            _tracker.floor = roomDTOMap.get( tracker.getRoomId() ).getFloor();
            _tracker.posX = tracker.getPosX();
            _tracker.posY = tracker.getPosY();

            result.add( _tracker );
        }
        return result;
    }
}
