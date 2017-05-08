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
	private static Log LOGGER = LogFactory.getLog(CacheService.class);

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
	}

	public long getLastUpdate()
	{
		return lastUpdate;
	}

	public List<TrackedBeacon> getTrackedBeacons()
	{
		List<TrackedBeacon> result = new ArrayList<>();

		List<List<TrackedBeacon>> trackedBeacons = new ArrayList<>(trackedBeaconMap.values());

		for( List<TrackedBeacon> beacons : trackedBeacons )
		{
			for( TrackedBeacon beacon : beacons )
			{
				result.add(beacon);
			}
		}
		return result;
	}

	public List<TrackedBeacon> getTrackedBeacons(Long beaconId)
	{
		List<TrackedBeacon> result = new ArrayList<>();

		List<List<TrackedBeacon>> trackedBeacons = new ArrayList<>(trackedBeaconMap.values());

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

	public Set<Long> getActiveRooms()
	{
		return activeRooms;
	}

	public List<TrackedBeacon> getActiveTrackedBeacons()
	{
		return new ArrayList<>(activeBeaconMap.values());
	}

	public void storeTrackerList(Long trackerId, List<TrackedBeacon> trackedBeacons)
	{
		trackedBeaconMap.put(trackerId, trackedBeacons);
		lastUpdate = System.currentTimeMillis();
	}

	public void updateActiveTracker()
	{
		List<BeaconDTO> beaconDTOs = daoCacheService.getBeacons();
		Map<Long, TrackerDTO> trackerDTOMap = daoCacheService.getTrackerIDMap();

		Set<Long> _activeRooms = new HashSet<>();

		for( BeaconDTO beaconDTO : beaconDTOs )
		{
			List<TrackedBeacon> trackedBeaconDTOs = getTrackedBeacons(beaconDTO.getId());

			// Determine the most relevant Tracker
			TrackedBeacon activeTracker = null;
			for( TrackedBeacon trackedBeaconDTO : trackedBeaconDTOs )
			{
				activeTracker = getActiveTracker(activeTracker, trackedBeaconDTO);
			}

			// get last active tracker
			TrackedBeacon lastActiveTracker = activeBeaconMap.get(beaconDTO.getId());

			// update "active" state
			if( activeTracker != null )
			{
				if( lastActiveTracker != null )
				{
					// active tracker is the same. keep old values and reset last "losing" tracker
					if( lastActiveTracker.trackerId.equals(activeTracker.trackerId) )
					{
						activeTracker.activeCount = lastActiveTracker.activeCount;
						activeTracker.attemptTrackerId = null;
						activeTracker.attemptTrackerCount = 0;
					}
					// active tracker is different
					else
					{
						// check if lastActiveTracker is still present
						TrackedBeacon prioritizedBeacon = findBeacon( trackedBeaconDTOs, lastActiveTracker );

						// prioritizedBeacon is used for the "isPriorisedActiveTracker" calculation
						if( prioritizedBeacon != null )
						{
							// use "trackedBeacon" if "lastActiveTracker" has higher priority then "activeTracker"
							if( isPriorisedActiveTracker(lastActiveTracker, prioritizedBeacon, activeTracker) )
							{
								// store different "losing" activeTracker
								if( !activeTracker.trackerId.equals(lastActiveTracker.attemptTrackerId) )
								{
									prioritizedBeacon.attemptTrackerId = activeTracker.trackerId;
									prioritizedBeacon.attemptTrackerCount = 0;
								}
								// keep "losing" activeTracker
								else
								{
									prioritizedBeacon.attemptTrackerId = lastActiveTracker.attemptTrackerId;
									prioritizedBeacon.attemptTrackerCount = lastActiveTracker.attemptTrackerCount;
								}
								// increase attempt count of "losing" activeTracker
								prioritizedBeacon.attemptTrackerCount++;

								prioritizedBeacon.activeCount = lastActiveTracker.activeCount;

								activeTracker = prioritizedBeacon;
							}
						}
						// fallback for a temporary missing trackedBeacon
						else if( lastActiveTracker.fallbackCount < CacheWatcherService.MAX_FALLBACK_COUNT )
						{
							if( isPriorisedActiveTracker(lastActiveTracker, activeTracker) )
							{
								// store "losing" activeTracker
								if( !activeTracker.trackerId.equals(lastActiveTracker.attemptTrackerId) )
								{
									lastActiveTracker.attemptTrackerId = activeTracker.trackerId;
									lastActiveTracker.attemptTrackerCount = 0;
								}
								// increase attempt count of "losing" activeTracker
								lastActiveTracker.attemptTrackerCount++;

								// disable priorised isActive check next time
								lastActiveTracker.fallbackCount++;

								activeTracker = lastActiveTracker;
							}
						}
					}
				}

				activeTracker.activeCount++;

				_activeRooms.add(trackerDTOMap.get(activeTracker.trackerId).getRoomId());

				activeBeaconMap.put(activeTracker.beaconId, activeTracker);
			}
			// remove lastActiveTracker
			else if( lastActiveTracker != null )
			{
				activeBeaconMap.remove(lastActiveTracker.trackerId);
			}
		}

		activeRooms = _activeRooms;
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

	private boolean isPriorisedActiveTracker(TrackedBeacon lastActiveTrackerRef, TrackedBeacon newActiveTracker)
	{
		return isPriorisedActiveTracker(lastActiveTrackerRef, lastActiveTrackerRef, newActiveTracker);
	}

	private boolean isPriorisedActiveTracker(TrackedBeacon lastActiveTrackerRef, TrackedBeacon currentActiveTracker, TrackedBeacon newActiveTracker)
	{
		if( lastActiveTrackerRef.activeCount >= CacheWatcherService.ACTIVE_COUNT_THRESHOLD )
		{
			if( newActiveTracker.trackerId.equals(lastActiveTrackerRef.attemptTrackerId) )
			{
				// new tracker is trying more then one time
				if( lastActiveTrackerRef.attemptTrackerCount >= CacheWatcherService.FORCE_NORMAL_CHECK_ATTEMPT_THRESHOLD )
				{
					return false;
				}
			}

			if( newActiveTracker.samples > CacheWatcherService.MIN_SAMPLE_THRESHOLD )
			{
				// new tracker has a very good signal
				if( newActiveTracker.rssi > CacheWatcherService.FORCE_NORMAL_CHECK_RSSI_THRESHOLD )
				{

					return false;
				}
			}

			//if( currentActiveTracker.samples > CacheWatcherService.MIN_SAMPLE_THRESHOLD )
			// priorised trackers signal is higher then "reduced" new signal
			if( currentActiveTracker.rssi > (newActiveTracker.rssi - CacheWatcherService.FORCE_PRIORITY_CHECK_RSSI_THRESHOLD) )
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
}