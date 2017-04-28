package com.holgerhees.indoorpos.frontend.controller.overview;

import com.google.gson.JsonElement;
import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.indoorpos.persistance.dao.RoomDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.RoomDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.util.GSonFactory;
import com.holgerhees.shared.web.view.GsonView;
import com.holgerhees.shared.web.view.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component( "overviewTrackerController" )
public class OverviewTrackerController implements Controller
{
    @Autowired
    TrackerDAO trackerDAO;

    @Autowired
    RoomDAO roomDAO;

    private class Tracker
    {
        String name;
        int floor;
        int posX;
        int posY;
    }

    @Override
    public View handle( Request request )
    {
        Map<Long, RoomDTO> roomDTOMap = roomDAO.getRoomIDMap();
        List<TrackerDTO> trackers = trackerDAO.getTracker();

        List<Tracker> result = new ArrayList<>();
        for( TrackerDTO tracker : trackers )
        {
            Tracker _tracker = new Tracker();
            _tracker.name = tracker.getName();
            _tracker.floor = roomDTOMap.get( tracker.getRoomId() ).getFloor();
            _tracker.posX = tracker.getPosX();
            _tracker.posY = tracker.getPosY();

            result.add( _tracker );
        }

        JsonElement json = GSonFactory.createGSon().toJsonTree( result );

        return new GsonView( json, request );
    }
}
