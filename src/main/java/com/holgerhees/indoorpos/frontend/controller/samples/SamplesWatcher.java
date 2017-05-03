package com.holgerhees.indoorpos.frontend.controller.samples;

/**
 * Created by hhees on 03.05.17.
 */

import com.google.gson.JsonElement;
import com.holgerhees.indoorpos.frontend.service.CacheService;
import com.holgerhees.indoorpos.persistance.dao.BeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.indoorpos.util.TrackingHelper;
import com.holgerhees.shared.web.util.GSonFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component( "samplesWatcher" )
public class SamplesWatcher
{
    private static Log LOGGER = LogFactory.getLog( SamplesWatcher.class );

    @Autowired
    BeaconDAO beaconDAO;

    @Autowired
    TrackerDAO trackerDAO;

    @Autowired
    CacheService cacheService;

    private Thread watcher;

    private class Samples
    {
        String trackerName;
        String beaconName;
        boolean isActive;
        int rssi;
        int samples;
    }

    public class Watcher implements Runnable
    {
        public Watcher()
        {
            //Initialization of atributes
        }

        @Override
        public void run()
        {
            while( true )
            {
                try
                {
                    //LOGGER.info( "Samples watcher" );
                    Thread.sleep( 500 );

                    if( SamplesEndPoint.hasSessions() )
                    {
                        String json = getSamplesAsJson();
                        SamplesEndPoint.broadcast( json );
                    }
                } catch( InterruptedException e )
                {
                    e.printStackTrace();
                }
                // Do something
            }
        }
    }

    @PostConstruct
    public void init()
    {
        watcher = new Thread( new Watcher() );
        watcher.setDaemon( true );
        watcher.start();
    }

    private String getSamplesAsJson()
    {
        Map<Long, TrackerDTO> trackerDTOMap = trackerDAO.getTrackerIDMap();
        Map<Long, BeaconDTO> beaconDTOs = beaconDAO.getBeaconIDMap();

        List<CacheService.TrackedBeacon> trackedBeaconDTOs = cacheService.getTrackedBeacons();

        List<SamplesWatcher.Samples> entries = new ArrayList<>();
        CacheService.TrackedBeacon activeTracker = null;
        int activeIndex = -1;
        for( int i = 0; i < trackedBeaconDTOs.size(); i++ )
        {
            CacheService.TrackedBeacon trackedBeaconDTO = trackedBeaconDTOs.get( i );

            TrackerDTO trackerDTO = trackerDTOMap.get( trackedBeaconDTO.getTrackerId() );
            BeaconDTO beaconDTO = beaconDTOs.get( trackedBeaconDTO.getBeaconId() );

            if( activeTracker == null || TrackingHelper.compareTracker( activeTracker, trackedBeaconDTO ) > 0 )
            {
                activeTracker = trackedBeaconDTO;
                activeIndex = i;
            }

            SamplesWatcher.Samples _sample = new SamplesWatcher.Samples();
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

        return json.toString();
    }
}
