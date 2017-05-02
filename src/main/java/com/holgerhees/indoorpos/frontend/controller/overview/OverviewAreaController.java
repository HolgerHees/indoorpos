package com.holgerhees.indoorpos.frontend.controller.overview;

import com.google.gson.JsonElement;
import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.indoorpos.frontend.service.CacheService;
import com.holgerhees.indoorpos.persistance.dao.*;
import com.holgerhees.indoorpos.persistance.dto.*;
import com.holgerhees.indoorpos.util.TrackingHelper;
import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.util.GSonFactory;
import com.holgerhees.shared.web.view.GsonView;
import com.holgerhees.shared.web.view.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component( "overviewAreaController" )
public class OverviewAreaController implements Controller
{
    @Autowired
    AreaDAO areaDAO;

    @Autowired
    RoomDAO roomDAO;

    @Autowired
    BeaconDAO beaconDAO;

    @Autowired
    TrackerDAO trackerDAO;

    @Autowired
    TrackedBeaconDAO trackedBeaconDAO;

    @Autowired
    CacheService cacheService;

    private class Area
    {
        String key;
        int topLeftX;
        int topLeftY;
        int bottomRightX;
        int bottomRightY;
        int floor;
    }

    private class Result
    {
        int nextWakeup;
        int age;
        int interval;
        List<Area> entries;
    }

    @Override
    public View handle( Request request )
    {
        Map<Long, TrackerDTO> trackerDTOMap = trackerDAO.getTrackerIDMap();

        List<BeaconDTO> beaconDTOs = beaconDAO.getBeacons();

        List<Long> detectedRooms = new ArrayList<>();

        for( BeaconDTO beaconDTO: beaconDTOs )
        {
            List<TrackedBeaconDTO> trackedBeaconDTOs = trackedBeaconDAO.getTrackedBeacons( beaconDTO.getId() );
            TrackedBeaconDTO activeTracker = null;
            for( TrackedBeaconDTO trackedBeaconDTO: trackedBeaconDTOs )
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

        Map<Long, RoomDTO> roomDTOMap = roomDAO.getRoomIDMap();

        List<AreaDTO> areas = areaDAO.getAreas();

        List<Area> entries = new ArrayList<>();
        for( AreaDTO area : areas )
        {
            if( !detectedRooms.contains( area.getRoomId() ) )
            {
                continue;
            }

            Area _area = new Area();
            _area.key = "area" + area.getId();
            _area.topLeftX = area.getTopLeftX();
            _area.topLeftY = area.getTopLeftY();
            _area.bottomRightX = area.getBottomRightX();
            _area.bottomRightY = area.getBottomRightY();
            _area.floor = roomDTOMap.get( area.getRoomId() ).getFloor();

            entries.add( _area );
        }

        Result result = new Result();

        int interval = cacheService.getTrackerInterval();
        long age = new Date().getTime() - cacheService.getLastTrackerUpdate();

        result.interval = interval;
        result.age = (int) age;

        if( interval > 0 )
        {
            // allways 10% ~300ms later
            result.nextWakeup = (int) ( ( interval * 1.1 ) - age );

            if( result.nextWakeup < CacheService.MIN_INTERVAL )
            {
                result.nextWakeup = CacheService.MIN_INTERVAL;
            }
        }
        else
        {
            result.nextWakeup = CacheService.MIN_INTERVAL;
        }

        result.entries = entries;

        JsonElement json = GSonFactory.createGSon().toJsonTree( result );

        return new GsonView( json, request );
    }
}
