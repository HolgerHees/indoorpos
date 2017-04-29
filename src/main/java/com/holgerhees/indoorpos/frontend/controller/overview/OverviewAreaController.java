package com.holgerhees.indoorpos.frontend.controller.overview;

import com.google.gson.JsonElement;
import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.indoorpos.persistance.dao.AreaDAO;
import com.holgerhees.indoorpos.persistance.dao.DetectedRoomsDAO;
import com.holgerhees.indoorpos.persistance.dao.RoomDAO;
import com.holgerhees.indoorpos.persistance.dto.AreaDTO;
import com.holgerhees.indoorpos.persistance.dto.RoomDTO;
import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.util.GSonFactory;
import com.holgerhees.shared.web.view.GsonView;
import com.holgerhees.shared.web.view.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    DetectedRoomsDAO detectedRoomsDAO;

    private class Area
    {
        String key;
        int topLeftX;
        int topLeftY;
        int bottomRightX;
        int bottomRightY;
        int floor;
    }

    @Override
    public View handle( Request request )
    {
        List<Long> detectedRooms = detectedRoomsDAO.getDetectedRooms();

        Map<Long, RoomDTO> roomDTOMap = roomDAO.getRoomIDMap();

        List<AreaDTO> areas = areaDAO.getAreas();

        List<Area> result = new ArrayList<>();
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

            result.add( _area );
        }

        JsonElement json = GSonFactory.createGSon().toJsonTree( result );

        return new GsonView( json, request );
    }
}
