package com.holgerhees.indoorpos.frontend.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component( "cacheService" )
public class CacheService
{
    private long lastUpdate;

    public static class TrackedBeacon
    {
        private Long trackerId;
        private Long beaconId;
        private int txPower;
        private int rssi;
        private int samples;

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

    Map<Long, List<TrackedBeacon>> trackedBeaconMap = new HashMap<>();

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

    public void storeTrackerList( Long trackerId, List<TrackedBeacon> trackedBeacons )
    {
        trackedBeaconMap.put( trackerId, trackedBeacons );
        lastUpdate = System.currentTimeMillis();
    }

    public long getLastUpdate()
    {
        return lastUpdate;
    }
}
