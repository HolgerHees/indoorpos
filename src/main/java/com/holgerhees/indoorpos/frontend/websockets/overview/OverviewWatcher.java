package com.holgerhees.indoorpos.frontend.websockets.overview;

/**
 * Created by hhees on 03.05.17.
 */

import com.google.gson.JsonElement;
import com.holgerhees.indoorpos.frontend.service.CacheService;
import com.holgerhees.indoorpos.frontend.service.CacheServiceBuilderClient;
import com.holgerhees.indoorpos.frontend.service.CacheServiceBuilderJob;
import com.holgerhees.indoorpos.frontend.service.DAOCacheService;
import com.holgerhees.indoorpos.frontend.websockets.EndPointWatcherClient;
import com.holgerhees.indoorpos.persistance.dto.AreaDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.shared.web.util.GSonFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.Session;
import java.util.*;

@Component( "overviewWatcher" )
public class OverviewWatcher implements CacheServiceBuilderClient, EndPointWatcherClient
{
    private static Log LOGGER = LogFactory.getLog( OverviewWatcher.class );

    @Autowired
    DAOCacheService daoCacheService;

    @Autowired
    CacheService cacheService;

    @Autowired
    CacheServiceBuilderJob cacheWatcherService;

    private String lastMessage;

	public static class Result
	{
		String type;
		Object data;

		public Result( String type, Object data )
		{
			this.type = type;
			this.data = data;
		}
	}

	private class Beacon
	{
		String key;
		int x;
		int y;
		int floor;
	}

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
        OverviewEndPoint.setWatcher( this );
    }

    @Override
    public void notifyCacheChange()
    {
        if( !OverviewEndPoint.hasSessions() ) return;

        List<Long> detectedRooms = cacheService.getActiveRooms();

	    List<Result> result = new ArrayList<>();

        List<OverviewWatcher.Area> areas = getAreas( detectedRooms );
	    result.add( new Result( "area", areas ) );

	    List<Beacon> beacons = getBeacons();
	    result.add( new Result( "beacon", beacons ) );

	    JsonElement json = GSonFactory.createGSon().toJsonTree( result );

		String message = json.toString();
	    if( message.equals( lastMessage ) ) return;
	    lastMessage = message;

	    OverviewEndPoint.broadcastMessage( message );
    }

    @Override
    public void notifyNewSession( Session userSession )
    {
	    List<Result> result = new ArrayList<>();

	    result.add( new Result( "tracker", getTracker() ) );

	    List<Long> detectedRooms = cacheService.getActiveRooms();
        List<OverviewWatcher.Area> areas = getAreas( detectedRooms );
	    result.add( new Result( "area", areas ) );

	    List<Beacon> beacons = getBeacons();
	    result.add( new Result( "beacon", beacons ) );

	    JsonElement json = GSonFactory.createGSon().toJsonTree( result);

        OverviewEndPoint.sendMessage( userSession, json.toString() );
    }

	private List<Beacon> getBeacons()
	{
		List<Beacon> entries = new ArrayList<>();
		for( CacheService.BeaconPosition position: cacheService.getBeaconPositions() )
		{
			Beacon beacon = new Beacon();
			beacon.key = "beacon" + position.getBeaconId();
			beacon.x = position.getX();
			beacon.y = position.getY();
			beacon.floor = daoCacheService.getRoomById( position.getRoomId() ).getFloor();
		}

		return entries;
	}

    private List<Area> getAreas( List<Long> activeRooms )
    {
        List<AreaDTO> areas = daoCacheService.getAreas( activeRooms );

        List<Area> entries = new ArrayList<>();
        for( AreaDTO area : areas )
        {
            Area _area = new Area();
            _area.key = "area" + area.getId();
            _area.topLeftX = area.getTopLeftX();
            _area.topLeftY = area.getTopLeftY();
            _area.bottomRightX = area.getBottomRightX();
            _area.bottomRightY = area.getBottomRightY();
            _area.floor = daoCacheService.getRoomById( area.getRoomId() ).getFloor();

            entries.add( _area );
        }

        return entries;
    }

    private List<Tracker> getTracker()
    {
        List<TrackerDTO> trackers = daoCacheService.getTracker();

        List<Tracker> result = new ArrayList<>();
        for( TrackerDTO tracker : trackers )
        {
            Tracker _tracker = new Tracker();
            _tracker.key = "tracker" + tracker.getId();
            _tracker.name = tracker.getName();
            _tracker.floor = daoCacheService.getRoomById( tracker.getRoomId() ).getFloor();
            _tracker.posX = tracker.getPosX();
            _tracker.posY = tracker.getPosY();

            result.add( _tracker );
        }
        return result;
    }
}
