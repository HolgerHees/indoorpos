package com.holgerhees.indoorpos.frontend.websockets.samples;

/**
 * Created by hhees on 03.05.17.
 */

import com.holgerhees.indoorpos.frontend.service.CacheService;
import com.holgerhees.indoorpos.frontend.service.CacheWatcherClient;
import com.holgerhees.indoorpos.frontend.service.CacheWatcherService;
import com.holgerhees.indoorpos.frontend.service.DAOCacheService;
import com.holgerhees.indoorpos.frontend.websockets.EndPointWatcherClient;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.indoorpos.util.TrackingHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component( "samplesWatcher" )
public class SamplesWatcher implements CacheWatcherClient, EndPointWatcherClient
{
    private static Log LOGGER = LogFactory.getLog( SamplesWatcher.class );

    @Autowired
    DAOCacheService daoCacheService;

    @Autowired
    CacheService cacheService;

    @Autowired
    CacheWatcherService cacheWatcherService;

    private class Samples
    {
        String trackerName;
        String beaconName;
        boolean isActive;
        int rssi;
        int samples;
    }

    @PostConstruct
    public void init()
    {
        cacheWatcherService.addWatcher( this );
        SamplesEndPoint.setSamplesWatcher( this );
    }

    @Override
    public void notifyCacheChange()
    {
        if( !SamplesEndPoint.hasSessions() )
        {
            return;
        }

        List<SamplesWatcher.Samples> samples = getSamples();
        SamplesEndPoint.broadcastMessage( samples );
    }

    @Override
    public void notifyNewSession( Session userSession )
    {
        List<SamplesWatcher.Samples> samples = getSamples();
        SamplesEndPoint.broadcastMessage( samples );
    }

    private List<SamplesWatcher.Samples> getSamples()
    {
        Map<Long, TrackerDTO> trackerDTOMap = daoCacheService.getTrackerIDMap();
        Map<Long, BeaconDTO> beaconDTOMap = daoCacheService.getBeaconIDMap();
        Map<Long, CacheService.TrackedBeacon> activeTrackedBeaconMap = cacheService.getActiveTrackedBeaconMap();

        List<SamplesWatcher.Samples> entries = new ArrayList<>();
        List<CacheService.TrackedBeacon> trackedBeaconDTOs = cacheService.getTrackedBeacons();

        for( CacheService.TrackedBeacon trackedBeaconDTO: trackedBeaconDTOs )
        {
            TrackerDTO trackerDTO = trackerDTOMap.get( trackedBeaconDTO.getTrackerId() );
            BeaconDTO beaconDTO = beaconDTOMap.get( trackedBeaconDTO.getBeaconId() );

            CacheService.TrackedBeacon activeTrackedBeacon = activeTrackedBeaconMap.get( beaconDTO.getId() );

            SamplesWatcher.Samples _sample = new SamplesWatcher.Samples();
            _sample.trackerName = trackerDTO.getName();
            _sample.beaconName = beaconDTO.getName();
            _sample.rssi = trackedBeaconDTO.getRssi();
            _sample.samples = trackedBeaconDTO.getSamples();
            _sample.isActive = activeTrackedBeacon.getTrackerId().equals( trackedBeaconDTO.getTrackerId() );
            entries.add( _sample );
        }

        return entries;
    }
}
