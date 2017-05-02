package com.holgerhees.indoorpos.frontend.controller.samples;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.indoorpos.frontend.service.CacheService;
import com.holgerhees.indoorpos.persistance.dao.AreaDAO;
import com.holgerhees.indoorpos.persistance.dao.BeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.RoomDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackedBeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.AreaDTO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.RoomDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.util.GSonFactory;
import com.holgerhees.shared.web.view.GsonView;
import com.holgerhees.shared.web.view.View;

@Component( "samplesUpdateController" )
public class SamplesUpdateController implements Controller
{
    @Autowired
    BeaconDAO beaconDAO;

    @Autowired
    TrackerDAO trackerDAO;

    @Autowired
    TrackedBeaconDAO trackedBeaconDAO;

    private class Samples
    {
        String trackerName;
	    String beaconName;
        int rssi;
        int samples;
	    int interval;
    }

    @Override
    public View handle( Request request )
    {
        Map<Long, TrackerDTO> trackerDTOMap = trackerDAO.getTrackerIDMap();

        List<TrackedBeaconDTO> trackedBeaconDTOs = trackedBeaconDAO.getActiveTrackedBeacons();

        Map<Long, BeaconDTO> beaconDTOs = beaconDAO.getBeaconIDMap();

        List<Samples> entries = new ArrayList<>();
        for( TrackedBeaconDTO trackedBeaconDTO : trackedBeaconDTOs )
        {
	        TrackerDTO trackerDTO = trackerDTOMap.get( trackedBeaconDTO.getTrackerId() );
	        BeaconDTO beaconDTO = beaconDTOs.get( trackedBeaconDTO.getBeaconId() );

	        Samples _area = new Samples();
            _area.trackerName = trackerDTO.getName();
	        _area.beaconName = beaconDTO.getName();
            _area.rssi = trackedBeaconDTO.getRssi();
	        _area.samples = trackedBeaconDTO.getSamples();
	        _area.interval = trackedBeaconDTO.getInterval();

            entries.add( _area );
        }

        JsonElement json = GSonFactory.createGSon().toJsonTree( entries );

        return new GsonView( json, request );
    }
}
