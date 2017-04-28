package com.holgerhees.indoorpos.frontend.controller.overview;

import com.google.gson.JsonElement;
import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.indoorpos.persistance.dao.BeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.RoomDAO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
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

@Component( "overviewBeaconController" )
public class OverviewBeaconController implements Controller
{
    @Autowired
    BeaconDAO beaconDAO;

    @Autowired
    RoomDAO roomDAO;

    private class Beacon
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
        List<BeaconDTO> beacons = beaconDAO.getBeacons();

        List<Beacon> result = new ArrayList<>();
        for( BeaconDTO beacon : beacons )
        {
            Beacon _beacon = new Beacon();
            _beacon.name = beacon.getName();
            _beacon.floor = beacon.getRoomId() == null ? -1 : roomDTOMap.get( beacon.getRoomId() ).getFloor();
            _beacon.posX = beacon.getPosX();
            _beacon.posY = beacon.getPosY();

            result.add( _beacon );
        }

        JsonElement json = GSonFactory.createGSon().toJsonTree( result );

        return new GsonView( json, request );
    }
}
