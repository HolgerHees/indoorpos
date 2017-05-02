package com.holgerhees.indoorpos.frontend.controller.samples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.holgerhees.indoorpos.util.TrackingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.indoorpos.persistance.dao.BeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackedBeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
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
	    boolean isActive;
        int rssi;
        int samples;
    }

    @Override
    public View handle( Request request )
    {
        Map<Long, TrackerDTO> trackerDTOMap = trackerDAO.getTrackerIDMap();

        List<TrackedBeaconDTO> trackedBeaconDTOs = trackedBeaconDAO.getTrackedBeacons();

        Map<Long, BeaconDTO> beaconDTOs = beaconDAO.getBeaconIDMap();

        List<Samples> entries = new ArrayList<>();
        TrackedBeaconDTO activeTracker = null;
        int activeIndex = -1;
        for( int i = 0; i < trackedBeaconDTOs.size(); i++ )
        {
            TrackedBeaconDTO trackedBeaconDTO = trackedBeaconDTOs.get( i );

	        TrackerDTO trackerDTO = trackerDTOMap.get( trackedBeaconDTO.getTrackerId() );
	        BeaconDTO beaconDTO = beaconDTOs.get( trackedBeaconDTO.getBeaconId() );

            if( activeTracker == null || TrackingHelper.compareTracker( activeTracker, trackedBeaconDTO ) > 0 )
            {
                activeTracker = trackedBeaconDTO;
                activeIndex = i;
            }

	        Samples _sample = new Samples();
            _sample.trackerName = trackerDTO.getName();
            _sample.beaconName = beaconDTO.getName();
            _sample.rssi = trackedBeaconDTO.getRssi();
            _sample.samples = trackedBeaconDTO.getSamples();

            entries.add( _sample );
        }

        if( activeIndex >= 0 )
        {
            entries.get( activeIndex ).isActive = true;
        }

        JsonElement json = GSonFactory.createGSon().toJsonTree( entries );

        return new GsonView( json, request );
    }
}
