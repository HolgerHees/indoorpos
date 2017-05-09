package com.holgerhees.indoorpos.frontend.websockets.tracker;

/**
 * Created by hhees on 03.05.17.
 */

import com.holgerhees.indoorpos.frontend.service.CacheService;
import com.holgerhees.indoorpos.frontend.service.CacheServiceBuilderJob;
import com.holgerhees.indoorpos.frontend.service.DAOCacheService;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.shared.web.util.GSonFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Component( "trackerWatcher" )
public class TrackerWatcher
{
    private static Log LOGGER = LogFactory.getLog( TrackerWatcher.class );
    private static DecimalFormat df = new DecimalFormat( "#.####" );

    private class TrackedBeaconSample
    {
        private int txpower;
        private int rssi;
        private double timestamp;
    }

    private class TrackedBeacon
    {
        private String mac;
        private String uuid;
        private String major;
        private String minor;
        private List<TrackedBeaconSample> samples;
    }

    private class Parameter
    {
        private String uuid;
        private List<TrackedBeacon> trackedBeacons;
    }

    @Autowired
    private DAOCacheService daoCacheService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private CacheServiceBuilderJob cacheWatcherService;

    @PostConstruct
    public void init()
    {
        TrackerEndPoint.setTrackerWatcher( this );
    }

    public void notifyTrackerChange( String message )
    {
        long start = System.currentTimeMillis();

        Parameter param = GSonFactory.createGSon().fromJson( message, Parameter.class );

        TrackerDTO trackerDTO = daoCacheService.getTrackerByUUID( param.uuid );

        if( trackerDTO == null )
        {
            LOGGER.info( "Skip unknown tracker with 'uuid': " + param.uuid );
        }
        else
        {
            processTrackedBeacons( param, trackerDTO );

            LOGGER.info( "Handle tracker message in " + df
                    .format( ( ( System.currentTimeMillis() - start ) / 1000.0f ) ) + " seconds (" + trackerDTO.getName() + ")" );
        }
    }

    public long getNextWakeup()
    {
        return cacheWatcherService.getNextWakeup();
    }

    public TrackerDTO getTrackerByIp( String ip )
    {
        return daoCacheService.getTrackerByIp( ip );
    }

    private void processTrackedBeacons( Parameter param, TrackerDTO trackerDTO )
    {
        List<CacheService.TrackedBeacon> trackedBeacons = new ArrayList<>();

        for( TrackedBeacon beacon : param.trackedBeacons )
        {
            BeaconDTO beaconDTO = daoCacheService.getBeaconByUUID( beacon.uuid );

            if( beaconDTO == null )
            {
                //LOGGER.info( "Skip unknown beacon with 'uuid': " + beacon.uuid );
                continue;
            }

            int txpower = 0;
            int rssi = 0;
            for( TrackedBeaconSample sample : beacon.samples )
            {
                txpower += sample.txpower;
                rssi += sample.rssi + trackerDTO.getRssiOffset();
            }
            int size = beacon.samples.size();
            txpower = txpower / size;
            rssi = rssi / size;

            //LOGGER.info( "Tracker " + trackerDTO.getName() + ". RSSI: " + rssi + ", Samples: " + size );

            CacheService.TrackedBeacon trackedBeacon = new CacheService.TrackedBeacon();
            trackedBeacon.setTrackerId( trackerDTO.getId() );
            trackedBeacon.setBeaconId( beaconDTO.getId() );
            trackedBeacon.setTxPower( txpower );
            trackedBeacon.setRssi( rssi );
            trackedBeacon.setSamples( size );

            trackedBeacons.add( trackedBeacon );
        }

        cacheService.storeTrackerList( trackerDTO.getId(), trackedBeacons );
    }
}
