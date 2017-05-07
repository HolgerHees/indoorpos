package com.holgerhees.indoorpos.frontend.service;

import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component( "cacheService" )
public class CacheService
{
    private static Log LOGGER = LogFactory.getLog( CacheService.class );

    private long lastUpdate;
    Map<Long, List<TrackedBeacon>> trackedBeaconMap = new HashMap<>();
    Map<Long, TrackedBeacon> activeBeaconMap = new HashMap<>();
    Set<Long> activeRooms = new HashSet<>();

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

        public boolean isActive()
        {
            return this.activeCount > 0;
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

    public Set<Long> getActiveRooms()
    {
        return activeRooms;
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
        Map<Long, TrackerDTO> trackerDTOMap = daoCacheService.getTrackerIDMap();

        Set<Long> _activeRooms = new HashSet<>(  );

        for( BeaconDTO beaconDTO : beaconDTOs )
        {
            List<TrackedBeacon> trackedBeaconDTOs = getTrackedBeacons( beaconDTO.getId() );

            // get last active tracker
            TrackedBeacon lastActiveTracker = activeBeaconMap.get( beaconDTO.getId() );
            // and check if it is still tracked
            TrackedBeacon lastActiveTrackerStillActive = null;
            if( lastActiveTracker != null )
            {
                for( TrackedBeacon trackedBeacon : trackedBeaconDTOs )
                {
                    if( trackedBeacon.getTrackerId().equals( lastActiveTracker.trackerId ) )
                    {
                        lastActiveTrackerStillActive = lastActiveTracker;
                        break;
                    }
                }
            }

            // Determine the most relevant Tracker
            TrackedBeacon activeTracker = null;
            for( TrackedBeacon trackedBeaconDTO : trackedBeaconDTOs )
            {
                activeTracker = getActiveTracker( lastActiveTrackerStillActive, activeTracker, trackedBeaconDTO );
            }

            // update "active" state
            if( activeTracker != null )
            {
                // remove lastActiveTracker if the new activeTracker is different
                if( lastActiveTracker != null )
                {
                    if( !lastActiveTracker.trackerId.equals( activeTracker.trackerId ) )
                    {
                        activeBeaconMap.remove( lastActiveTracker.trackerId );
                    }
                    else
                    {
                        activeTracker.activeCount = lastActiveTracker.activeCount;
                    }
                }

                activeTracker.activeCount++;

                _activeRooms.add( trackerDTOMap.get( activeTracker.trackerId ).getRoomId() );

                activeBeaconMap.put( activeTracker.beaconId, activeTracker );
            }
            // remove lastActiveTracker
            else if( lastActiveTracker != null )
            {
                activeBeaconMap.remove( lastActiveTracker.trackerId );
            }
        }

        activeRooms = _activeRooms;
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
            if( lastActiveBeacon.trackerId.equals( t1.trackerId ) )
            {
                if( isActive( lastActiveBeacon.activeCount, t1, t2 ) ) return t1;
            }
            else if( lastActiveBeacon.trackerId.equals( t2.trackerId ) )
            {
                if( isActive( lastActiveBeacon.activeCount, t2, t1 ) ) return t2;
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

    private boolean isActive( int activeCount, TrackedBeacon t1, TrackedBeacon t2 )
    {
        if( activeCount > CacheWatcherService.ACTIVE_COUNT_THRESHOLD )
        {
            if( t2.samples > CacheWatcherService.MIN_SAMPLE_THRESHOLD )
            {
                if( t2.rssi > CacheWatcherService.FORCE_NORMAL_CHECK_THRESHOLD ) return false;
            }

            if( t1.samples > CacheWatcherService.MIN_SAMPLE_THRESHOLD )
            {
                if( t1.rssi >= ( t2.rssi - CacheWatcherService.FORCE_PRIORITY_CHECK_THRESHOLD ) ) return true;
            }
        }
        return false;
    }
}
