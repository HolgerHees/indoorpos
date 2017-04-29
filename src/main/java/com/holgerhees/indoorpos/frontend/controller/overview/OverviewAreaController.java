package com.holgerhees.indoorpos.frontend.controller.overview;

import com.google.gson.JsonElement;
import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.indoorpos.persistance.dao.*;
import com.holgerhees.indoorpos.persistance.dto.AreaDTO;
import com.holgerhees.indoorpos.persistance.dto.RoomDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
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
    TrackerDAO trackerDAO;

    @Autowired
    TrackedBeaconDAO trackedBeaconDAO;

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
        Long age;
        List<Area> entries;
    }

    @Override
    public View handle( Request request )
    {
        Map<Long, TrackerDTO> trackerDTOMap = trackerDAO.getTrackerIDMap();

        List<TrackedBeaconDTO> trackedBeaconDTOs = trackedBeaconDAO.getActiveTrackedBeacons();
        List<Long> detectedRooms = new ArrayList<>();
        Date lastModified = null;
        for( TrackedBeaconDTO trackedBeaconDTO: trackedBeaconDTOs )
        {
            TrackerDTO trackerDTO = trackerDTOMap.get( trackedBeaconDTO.getTrackerId() );
            detectedRooms.add( trackerDTO.getRoomId() );

            if( lastModified == null || lastModified.before( trackedBeaconDTO.getLastModified() ) )
            {
                lastModified = trackedBeaconDTO.getLastModified();
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
        result.age = ( new Date().getTime() - lastModified.getTime() );
        result.entries = entries;

        JsonElement json = GSonFactory.createGSon().toJsonTree( result );

        return new GsonView( json, request );
    }
}
