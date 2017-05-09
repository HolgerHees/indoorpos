package com.holgerhees.indoorpos.frontend.service;

import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component( "cacheService" )
public class CacheService
{
	private static Log LOGGER = LogFactory.getLog(CacheService.class);

	private long lastUpdate;
	Map<Long, List<TrackedBeacon>> trackedBeaconsByTrackerIdMap = new HashMap<>();

	Map<Long, TrackedBeacon> activeBeaconByBeaconIdMap = new HashMap<>();
	Set<Long> activeRooms = new HashSet<>();
	List<TrackedBeacon> usedTrackedBeacons = new ArrayList<>();

	@Autowired
	DAOCacheService daoCacheService;

	public static class TrackedBeacon
	{
		private Long trackerId;
		private Long beaconId;
		private int txPower;
		private int rssi;
		private int samples;

		private int fallbackCount = 0;
		private int activeCount = 0;
		private int attemptTrackerCount = 0;
		private Long attemptTrackerId;

		public Long getTrackerId()
		{
			return trackerId;
		}

		public void setTrackerId(Long trackerId)
		{
			this.trackerId = trackerId;
		}

		public Long getBeaconId()
		{
			return beaconId;
		}

		public void setBeaconId(Long beaconId)
		{
			this.beaconId = beaconId;
		}

		public int getTxPower()
		{
			return txPower;
		}

		public void setTxPower(int txPower)
		{
			this.txPower = txPower;
		}

		public int getRssi()
		{
			return rssi;
		}

		public void setRssi(int rssi)
		{
			this.rssi = rssi;
		}

		public int getSamples()
		{
			return samples;
		}

		public void setSamples(int samples)
		{
			this.samples = samples;
		}

		public boolean isActive()
		{
			return this.activeCount > 0;
		}

		public boolean isFallback() { return this.fallbackCount > 0; }
	}

	public long getLastUpdate()
	{
		return lastUpdate;
	}

	public List<TrackedBeacon> getUsedTrackedBeacons()
	{
		return usedTrackedBeacons;
	}

	public Set<Long> getActiveRooms()
	{
		return activeRooms;
	}

	public void storeTrackerList(Long trackerId, List<TrackedBeacon> trackedBeacons)
	{
		trackedBeaconsByTrackerIdMap.put(trackerId, trackedBeacons);
		lastUpdate = System.currentTimeMillis();
	}

	public void updateActiveTracker()
	{
		List<BeaconDTO> beaconDTOs = daoCacheService.getBeacons();

		Set<Long> _activeRooms = new HashSet<>();
		List<TrackedBeacon> _usedTrackedBeacons = new ArrayList<>();

		for( BeaconDTO beaconDTO : beaconDTOs )
		{
			List<TrackedBeacon> trackedBeaconDTOs = getUsedTrackedBeacons(beaconDTO.getId());

			// Determine the most relevant Tracker
			TrackedBeacon newStrongestTrackedBeacon = null;
			for( TrackedBeacon trackedBeaconDTO : trackedBeaconDTOs )
			{
				newStrongestTrackedBeacon = getActiveTracker(newStrongestTrackedBeacon, trackedBeaconDTO);

				_usedTrackedBeacons.add( trackedBeaconDTO );
			}

			// get last active tracker
			TrackedBeacon lastStrongestTrackedBeacon = activeBeaconByBeaconIdMap.get(beaconDTO.getId());

			// update "active" state
			if( newStrongestTrackedBeacon != null )
			{
				// check if lastActiveTracker should stay as the active tracker
				newStrongestTrackedBeacon = checkLastActiveTrackerPriorised( lastStrongestTrackedBeacon, newStrongestTrackedBeacon, trackedBeaconDTOs );

				activeBeaconByBeaconIdMap.put(newStrongestTrackedBeacon.beaconId, newStrongestTrackedBeacon);
			}
			else if( lastStrongestTrackedBeacon != null )
			{
				// fallback for a temporary missing trackedBeacon
				if( isLastActiveTrackerPriorised( lastStrongestTrackedBeacon ) )
				{
					lastStrongestTrackedBeacon.attemptTrackerId = null;
					lastStrongestTrackedBeacon.attemptTrackerCount = 0;

					// disable priorised isActive check next time
					lastStrongestTrackedBeacon.fallbackCount++;

					newStrongestTrackedBeacon = lastStrongestTrackedBeacon;
				}
				// remove lastActiveTracker
				else
				{
					activeBeaconByBeaconIdMap.remove(lastStrongestTrackedBeacon.trackerId);
				}
			}

			// last active Tracker is only returned if it was not found in tracked Beacons
			if( newStrongestTrackedBeacon != null )
			{
				newStrongestTrackedBeacon.activeCount++;
				_activeRooms.add( daoCacheService.getTrackerById( newStrongestTrackedBeacon.trackerId ).getRoomId());

				// lastActiveTracker is only equal to activeTracker if it was not found in trackedBeacons
				if( newStrongestTrackedBeacon == lastStrongestTrackedBeacon )
				{
					_usedTrackedBeacons.add(lastStrongestTrackedBeacon);
				}
			}
		}

		activeRooms = _activeRooms;
		usedTrackedBeacons = _usedTrackedBeacons;
	}

	private TrackedBeacon checkLastActiveTrackerPriorised(TrackedBeacon lastStrongestTrackedBeacon, TrackedBeacon strongestTrackedBeacon,
		List<TrackedBeacon> trackedBeaconDTOs)
	{
		if( lastStrongestTrackedBeacon != null )
		{
			// active tracker is the same. keep old values and reset last "losing" tracker
			if( lastStrongestTrackedBeacon.trackerId.equals(strongestTrackedBeacon.trackerId) )
			{
				strongestTrackedBeacon.activeCount = lastStrongestTrackedBeacon.activeCount;
				strongestTrackedBeacon.attemptTrackerId = null;
				strongestTrackedBeacon.attemptTrackerCount = 0;
			}
			// active tracker is different
			else
			{
				// check if lastActiveTracker is still present
				TrackedBeacon _lastStrongestTrackedBeacon = findBeacon( trackedBeaconDTOs, lastStrongestTrackedBeacon );

				// prioritizedBeacon is used for the "isPriorisedActiveTracker" calculation
				if( _lastStrongestTrackedBeacon != null )
				{
					// use "trackedBeacon" if "lastActiveTracker" has higher priority then "activeTracker"
					if( isLastActiveTrackerPriorised(lastStrongestTrackedBeacon, _lastStrongestTrackedBeacon, strongestTrackedBeacon) )
					{
						// store different "losing" activeTracker
						if( !strongestTrackedBeacon.trackerId.equals(lastStrongestTrackedBeacon.attemptTrackerId) )
						{
							_lastStrongestTrackedBeacon.attemptTrackerId = strongestTrackedBeacon.trackerId;
							_lastStrongestTrackedBeacon.attemptTrackerCount = 0;
						}
						// keep "losing" activeTracker
						else
						{
							_lastStrongestTrackedBeacon.attemptTrackerId = lastStrongestTrackedBeacon.attemptTrackerId;
							_lastStrongestTrackedBeacon.attemptTrackerCount = lastStrongestTrackedBeacon.attemptTrackerCount;
						}
						// increase attempt count of "losing" activeTracker
						_lastStrongestTrackedBeacon.attemptTrackerCount++;

						_lastStrongestTrackedBeacon.activeCount = lastStrongestTrackedBeacon.activeCount;

						strongestTrackedBeacon = _lastStrongestTrackedBeacon;
					}
				}
				else
				{
					// fallback for a temporary missing trackedBeacon
					if( isLastActiveTrackerPriorised(lastStrongestTrackedBeacon, strongestTrackedBeacon) )
					{
						// store "losing" activeTracker
						if( !strongestTrackedBeacon.trackerId.equals(lastStrongestTrackedBeacon.attemptTrackerId) )
						{
							lastStrongestTrackedBeacon.attemptTrackerId = strongestTrackedBeacon.trackerId;
							lastStrongestTrackedBeacon.attemptTrackerCount = 0;
						}
						// increase attempt count of "losing" activeTracker
						lastStrongestTrackedBeacon.attemptTrackerCount++;

						// disable priorised isActive check next time
						lastStrongestTrackedBeacon.fallbackCount++;

						strongestTrackedBeacon = lastStrongestTrackedBeacon;
					}
				}
			}
		}

		return strongestTrackedBeacon;
	}

	private boolean isLastActiveTrackerPriorised(TrackedBeacon lastStrongestTrackedBeacon)
	{
		if( lastStrongestTrackedBeacon.activeCount >= CacheWatcherService.ACTIVE_COUNT_THRESHOLD
			&&
			lastStrongestTrackedBeacon.fallbackCount < CacheWatcherService.MAX_FALLBACK_COUNT )
		{
			return true;
		}

		return false;
	}

	private boolean isLastActiveTrackerPriorised(TrackedBeacon lastStrongestTrackedBeacon, TrackedBeacon newStrongestTrackedBeacon)
	{
		if( lastStrongestTrackedBeacon.fallbackCount < CacheWatcherService.MAX_FALLBACK_COUNT )
		{
			return isLastActiveTrackerPriorised(lastStrongestTrackedBeacon, lastStrongestTrackedBeacon, newStrongestTrackedBeacon);
		}

		return false;
	}

	private boolean isLastActiveTrackerPriorised(TrackedBeacon lastStrongestTrackedBeacon, TrackedBeacon _lastStrongestTrackedBeacon, TrackedBeacon newStrongestTrackedBeacon)
	{
		if( lastStrongestTrackedBeacon.activeCount >= CacheWatcherService.ACTIVE_COUNT_THRESHOLD )
		{
			if( newStrongestTrackedBeacon.trackerId.equals(lastStrongestTrackedBeacon.attemptTrackerId) )
			{
				// new tracker is trying more then one time
				if( lastStrongestTrackedBeacon.attemptTrackerCount >= CacheWatcherService.FORCE_NORMAL_CHECK_ATTEMPT_THRESHOLD )
				{
					return false;
				}
			}

			if( newStrongestTrackedBeacon.samples > CacheWatcherService.MIN_SAMPLE_THRESHOLD )
			{
				// new tracker has a very good signal
				if( newStrongestTrackedBeacon.rssi > CacheWatcherService.FORCE_NORMAL_CHECK_RSSI_THRESHOLD )
				{

					return false;
				}
			}

			//if( currentActiveTracker.samples > CacheWatcherService.MIN_SAMPLE_THRESHOLD )
			// priorised trackers signal is higher then "reduced" new signal
			if( _lastStrongestTrackedBeacon.rssi > (newStrongestTrackedBeacon.rssi - CacheWatcherService.FORCE_PRIORITY_CHECK_RSSI_THRESHOLD) )
			{
				return true;
			}
		}
		return false;
	}

	private TrackedBeacon findBeacon( List<TrackedBeacon> trackedBeaconDTOs, TrackedBeacon toFind )
	{
		for( TrackedBeacon trackedBeacon : trackedBeaconDTOs )
		{
			if( toFind.trackerId.equals(trackedBeacon.trackerId) )
			{
				return trackedBeacon;
			}
		}

		return null;
	}

	private List<TrackedBeacon> getUsedTrackedBeacons(Long beaconId)
	{
		List<TrackedBeacon> result = new ArrayList<>();

		List<List<TrackedBeacon>> trackedBeacons = new ArrayList<>(trackedBeaconsByTrackerIdMap.values());

		for( List<TrackedBeacon> beacons : trackedBeacons )
		{
			for( TrackedBeacon beacon : beacons )
			{
				if( !beacon.getBeaconId().equals(beaconId) )
				{
					continue;
				}
				result.add(beacon);
			}
		}
		return result;
	}

	private TrackedBeacon getActiveTracker(TrackedBeacon t1, TrackedBeacon t2)
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

        int signalStrengh1 = 100 + t1.getRssi();
        int priority1 = signalStrengh1 * t1.getSamples();

        int signalStrengh2 = 100 + t2.getRssi();
        int priority2 = signalStrengh2 * t2.getSamples();

        if( priority1 > priority2 )
            return t1;
        if( priority1 < priority2 )
            return t2;
        return t1;
    }
}