package com.holgerhees.indoorpos.frontend.websockets.overview;

/**
 * Created by hhees on 03.05.17.
 */

import com.holgerhees.indoorpos.frontend.service.CacheService;
import com.holgerhees.indoorpos.frontend.service.CacheServiceBuilderClient;
import com.holgerhees.indoorpos.frontend.service.CacheServiceBuilderJob;
import com.holgerhees.indoorpos.frontend.service.DAOCacheService;
import com.holgerhees.indoorpos.frontend.websockets.EndPointWatcherClient;
import com.holgerhees.indoorpos.persistance.dto.AreaDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
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
        OverviewEndPoint.setWatcher( this );
    }

    @Override
    public void notifyCacheChange()
    {
        if( !OverviewEndPoint.hasSessions() )
        {
            return;
        }

        List<Long> detectedRooms = cacheService.getActiveRooms();
        if( detectedRooms.equals( lastDetectedRooms ) )
        {
            return;
        }

        lastDetectedRooms = detectedRooms;

        List<OverviewWatcher.Area> areas = getAreas( detectedRooms );
        OverviewEndPoint.broadcastMessage( "area", areas );
    }

    @Override
    public void notifyNewSession( Session userSession )
    {
        OverviewEndPoint.sendMessage( userSession, "tracker", getTracker() );

	    List<Long> detectedRooms = cacheService.getActiveRooms();
        List<OverviewWatcher.Area> areas = getAreas( detectedRooms );
        OverviewEndPoint.sendMessage( userSession, "area", areas );
    }

    private List<OverviewWatcher.Area> getAreas( List<Long> activeRooms )
    {
        List<AreaDTO> areas = daoCacheService.getAreas( activeRooms );

        List<OverviewWatcher.Area> entries = new ArrayList<>();
        for( AreaDTO area : areas )
        {
            OverviewWatcher.Area _area = new OverviewWatcher.Area();
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

    private List<OverviewWatcher.Tracker> getTracker()
    {
        List<TrackerDTO> trackers = daoCacheService.getTracker();

        List<OverviewWatcher.Tracker> result = new ArrayList<>();
        for( TrackerDTO tracker : trackers )
        {
            OverviewWatcher.Tracker _tracker = new OverviewWatcher.Tracker();
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
