package com.holgerhees.indoorpos.frontend.service;

import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component( "cacheService" )
public class CacheService
{
    private static Log LOGGER = LogFactory.getLog( CacheService.class );

    private long lastUpdate;
    Map<Long, List<TrackedBeacon>> trackedBeaconMap = new HashMap<>();
    Map<Long, TrackedBeacon> activeBeaconMap = new HashMap<>();

    @Autowired
    DAOCacheService daoCacheService;

    public static class TrackedBeacon
    {
        private Long trackerId;
        private Long beaconId;
        private int txPower;
        private int rssi;
        private int samples;

        private int activeCount = 0;

        public Long getTrackerId()
        {
            return trackerId;
        }

        public void setTrackerId( Long trackerId )
        {
            this.trackerId = trackerId;
        }

        public Long getBeaconId()
        {
            return beaconId;
        }

        public void setBeaconId( Long beaconId )
        {
            this.beaconId = beaconId;
        }

        public int getTxPower()
        {
            return txPower;
        }

        public void setTxPower( int txPower )
        {
            this.txPower = txPower;
        }

        public int getRssi()
        {
            return rssi;
        }

        public void setRssi( int rssi )
        {
            this.rssi = rssi;
        }

        public int getSamples()
        {
            return samples;
        }

        public void setSamples( int samples )
        {
            this.samples = samples;
        }
    }

    public long getLastUpdate()
    {
        return lastUpdate;
    }

    public List<TrackedBeacon> getTrackedBeacons()
    {
        List<TrackedBeacon> result = new ArrayList<>();

        List<List<TrackedBeacon>> trackedBeacons = new ArrayList<>( trackedBeaconMap.values() );

        for( List<TrackedBeacon> beacons : trackedBeacons )
        {
            for( TrackedBeacon beacon : beacons )
            {
                result.add( beacon );
            }
        }
        return result;
    }

    public List<TrackedBeacon> getTrackedBeacons( Long beaconId )
    {
        List<TrackedBeacon> result = new ArrayList<>();

        List<List<TrackedBeacon>> trackedBeacons = new ArrayList<>( trackedBeaconMap.values() );

        for( List<TrackedBeacon> beacons : trackedBeacons )
        {
            for( TrackedBeacon beacon : beacons )
            {
                if( !beacon.getBeaconId().equals( beaconId ) )
                {
                    continue;
                }
                result.add( beacon );
            }
        }
        return result;
    }

    public Map<Long, TrackedBeacon> getActiveTrackedBeaconMap()
    {
        return activeBeaconMap;
    }

    public List<TrackedBeacon> getActiveTrackedBeacons()
    {
        return new ArrayList<>( activeBeaconMap.values() );
    }

    public void storeTrackerList( Long trackerId, List<TrackedBeacon> trackedBeacons )
    {
        trackedBeaconMap.put( trackerId, trackedBeacons );
        lastUpdate = System.currentTimeMillis();
    }

    public void updateActiveTracker()
    {
        List<BeaconDTO> beaconDTOs = daoCacheService.getBeacons();

        for( BeaconDTO beaconDTO : beaconDTOs )
        {
            List<TrackedBeacon> trackedBeaconDTOs = getTrackedBeacons( beaconDTO.getId() );

            TrackedBeacon _lastActiveTracker = activeBeaconMap.get( beaconDTO.getId() );
            TrackedBeacon lastActiveTracker = null;
            if( _lastActiveTracker != null )
            {
                for( TrackedBeacon trackedBeacon : trackedBeaconDTOs )
                {
                    if( trackedBeacon.getTrackerId().equals( _lastActiveTracker.getTrackerId() ) )
                    {
                        lastActiveTracker = _lastActiveTracker;
                        break;
                    }
                }
            }
            TrackedBeacon activeTracker = null;
            for( TrackedBeacon trackedBeaconDTO : trackedBeaconDTOs )
            {
                activeTracker = getActiveTracker( lastActiveTracker, activeTracker, trackedBeaconDTO );
            }
            if( activeTracker != null )
            {
                activeTracker.activeCount++;

                activeBeaconMap.put( beaconDTO.getId(), activeTracker );
            }
            else
            {
                // no need to reset activeCount. TrackedBeacon objects are recreated with every tracker update
                activeBeaconMap.remove( beaconDTO.getId() );
            }
        }
    }

    private TrackedBeacon getActiveTracker( TrackedBeacon lastActiveBeacon, TrackedBeacon t1, TrackedBeacon t2 )
    {
        /*double length1 = LocationHelper.getDistance(  t1.getRssi(), t1.getTxPower() );
        double cmp1 = (100 - length1) * t1.getSamples();

        double length2 = LocationHelper.getDistance(  t2.getRssi(), t2.getTxPower() );
        double cmp2 = (100 - length2) * t2.getSamples();

        if( cmp1 > cmp2 ) return -1;
        if( cmp1 < cmp2 ) return 1;
        return 0;*/

        if( t1 == null )
        {
            return t2;
        }

        if( lastActiveBeacon != null )
        {
            if( lastActiveBeacon.trackerId.equals( t1.trackerId ) && lastActiveBeacon.beaconId.equals( t1.beaconId ) )
            {
                // copy activeCount from old to new oject
                t1.activeCount = lastActiveBeacon.activeCount;
                if( t1.activeCount > 5 )
                {
                    if( t1.rssi >= t2.rssi ) return t1;
                    if( t1.samples >= t2.samples ) return t1;
                }
            }
            else if( lastActiveBeacon.trackerId.equals( t2.trackerId ) && lastActiveBeacon.beaconId.equals( t2.beaconId ) )
            {
                // copy activeCount from old to new oject
                t2.activeCount = lastActiveBeacon.activeCount;
                if( t2.activeCount > 5 )
                {
                    if( t2.rssi >= t1.rssi ) return t2;
                    if( t2.samples >= t1.samples ) return t2;
                }
            }
        }

        int signalStrengh1 = 100 + t1.getRssi();
        int priority1 = signalStrengh1 * t1.getSamples();

        int signalStrengh2 = 100 + t2.getRssi();
        int priority2 = signalStrengh2 * t2.getSamples();

        if( priority1 > priority2 ) return t1;
        if( priority1 < priority2 ) return t2;
        return t1;
    }
}
