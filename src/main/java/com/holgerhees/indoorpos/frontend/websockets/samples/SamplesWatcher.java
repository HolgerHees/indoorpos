package com.holgerhees.indoorpos.frontend.websockets.samples;

/**
 * Created by hhees on 03.05.17.
 */

import com.google.gson.JsonElement;
import com.holgerhees.indoorpos.frontend.service.CacheService;
import com.holgerhees.indoorpos.frontend.service.CacheServiceBuilderClient;
import com.holgerhees.indoorpos.frontend.service.CacheServiceBuilderJob;
import com.holgerhees.indoorpos.frontend.service.DAOCacheService;
import com.holgerhees.indoorpos.frontend.websockets.EndPointWatcherClient;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.shared.web.util.GSonFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;

@Component( "samplesWatcher" )
public class SamplesWatcher implements CacheServiceBuilderClient, EndPointWatcherClient
{
    private static Log LOGGER = LogFactory.getLog( SamplesWatcher.class );

    @Autowired
    DAOCacheService daoCacheService;

    @Autowired
    CacheService cacheService;

    @Autowired
    CacheServiceBuilderJob cacheWatcherService;

    private class Samples
    {
        String trackerName;
        String beaconName;
        List<CacheService.State> states;
	    double adjustedRssi;
        double rssi;
        int samples;
        double variance;
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
        if( !SamplesEndPoint.hasSessions() ) return;

        List<SamplesWatcher.Samples> samples = getSamples();
	    JsonElement json = GSonFactory.createGSon().toJsonTree( samples);

        SamplesEndPoint.broadcastMessage( json.toString() );
    }

    @Override
    public void notifyNewSession( Session userSession )
    {
        List<SamplesWatcher.Samples> samples = getSamples();
	    JsonElement json = GSonFactory.createGSon().toJsonTree( samples);

        SamplesEndPoint.sendMessage( userSession, json.toString() );
    }

    private List<SamplesWatcher.Samples> getSamples()
    {
        List<SamplesWatcher.Samples> entries = new ArrayList<>();
        List<CacheService.TrackedBeacon> trackedBeaconDTOs = cacheService.getUsedTrackedBeacons();

        for( CacheService.TrackedBeacon trackedBeaconDTO : trackedBeaconDTOs )
        {
            TrackerDTO trackerDTO = daoCacheService.getTrackerById( trackedBeaconDTO.getTrackerId() );
            BeaconDTO beaconDTO = daoCacheService.getBeaconById( trackedBeaconDTO.getBeaconId() );

            SamplesWatcher.Samples _sample = new SamplesWatcher.Samples();
            _sample.trackerName = trackerDTO.getName();
            _sample.beaconName = beaconDTO.getName();
            _sample.rssi = trackedBeaconDTO.getRssi();
            _sample.adjustedRssi = trackedBeaconDTO.getAdjustedRssi();
            _sample.samples = trackedBeaconDTO.getSamples();
            _sample.variance = trackedBeaconDTO.getVariance();
            _sample.states = trackedBeaconDTO.getStates();
            entries.add( _sample );
        }

        return entries;
    }
}
