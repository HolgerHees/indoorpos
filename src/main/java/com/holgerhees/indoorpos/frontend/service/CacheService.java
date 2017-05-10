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

	List<Long> activeRooms = new ArrayList<>();
	Map<Long, Long> activeRoomsByBeaconId = new HashMap<>();
	Map<Long, TrackedBeacon> strongestBeaconByBeaconIdMap = new HashMap<>();
	List<TrackedBeacon> usedTrackedBeacons = new ArrayList<>();

	@Autowired
	DAOCacheService daoCacheService;

	private class PrepareResult
	{
		TrackedBeacon newStrongestTrackedBeacon;
		TrackedBeacon lastStrongestTrackedBeacon;
		boolean lastStillTracked;
	}

	public static class TrackedBeacon
	{
		private Long trackerId;
		private Long beaconId;
		private int txPower;
		private int rssi;
		private int samples;

		private int activeCount = 0;

		private int fallbackCount = 0;

		private int priorityCheckCount = 0;
		private Long priorityCheckTrackedBeaconId;

		private boolean tooFarAway;

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

		public boolean isTooFarAway()
		{
			return this.tooFarAway;
		}

		public boolean isActive()
		{
			return this.activeCount > 0;
		}

		public boolean isFallback() { return this.fallbackCount > 0; }

		public void trackPriorityCheck(TrackedBeacon newStrongestTrackedBeacon)
		{
			if( !newStrongestTrackedBeacon.trackerId.equals(priorityCheckTrackedBeaconId)  )
			{
				priorityCheckTrackedBeaconId = newStrongestTrackedBeacon.trackerId;
				priorityCheckCount = 0;
			}
			priorityCheckCount++;
		}
	}

	public long getLastUpdate()
	{
		return lastUpdate;
	}

	public List<TrackedBeacon> getUsedTrackedBeacons()
	{
		return usedTrackedBeacons;
	}

	public List<Long> getActiveRooms()
	{
		return activeRooms;
	}

	public Long getActiveRoom( Long beaconId )
	{
		return activeRoomsByBeaconId.get( beaconId );
	}

	public void storeTrackerList(Long trackerId, List<TrackedBeacon> trackedBeacons)
	{
		trackedBeaconsByTrackerIdMap.put(trackerId, trackedBeacons);
		lastUpdate = System.currentTimeMillis();
	}

	public void updateActiveTracker()
	{
		List<BeaconDTO> beaconDTOs = daoCacheService.getBeacons();

		List<TrackedBeacon> _usedTrackedBeacons = new ArrayList<>();
		Map<Long, TrackedBeacon> _strongestBeaconByBeaconIdMap = new HashMap<>();
		Map<Long, Long> _activeRoomByBeaconId = new HashMap<>();

		Map<Long, List<TrackedBeacon>> trackedBeaconsByBeaconId = getTrackedBeaconsByBeaconIdMap();

		for( BeaconDTO beaconDTO : beaconDTOs )
		{
			// get last active tracker
			TrackedBeacon lastStrongestTrackedBeacon = strongestBeaconByBeaconIdMap.get(beaconDTO.getId());

			// STEP 1
			// detect NEW STRONGEST TRACKED BEACON (incl. possible rooms check)
			// update LAST STRONGEST TRACKED BEACON
			// check if LAST STRONGEST TRACKED BEACON is still tracked
			PrepareResult _result = prepareTrackedBeacons( lastStrongestTrackedBeacon, trackedBeaconsByBeaconId, _usedTrackedBeacons );
			TrackedBeacon newStrongestTrackedBeacon = _result.newStrongestTrackedBeacon;
			lastStrongestTrackedBeacon = _result.lastStrongestTrackedBeacon;
			boolean lastStillTracked = _result.lastStillTracked;

			// STEP 2
			// check if LAST STRONGEST TRACKED BEACON has higher priority that NEW STRONGEST TRACKED BEACON
			if( newStrongestTrackedBeacon != null )
			{
				newStrongestTrackedBeacon = checkLastActiveTrackerPriorised( lastStillTracked, lastStrongestTrackedBeacon, newStrongestTrackedBeacon );
			}
			// check if LAST STRONGEST TRACKED BEACON is a temporary fallback
			else if( lastStrongestTrackedBeacon != null )
			{
				newStrongestTrackedBeacon = checkLastActiveTrackerPriorisedAsFallback( lastStrongestTrackedBeacon );
			}

			// STEP 3
			// last active Tracker is only returned if it was not found in tracked Beacons
			if( newStrongestTrackedBeacon != null )
			{
				if( lastStrongestTrackedBeacon == newStrongestTrackedBeacon && !lastStillTracked )
				{
					_usedTrackedBeacons.add(lastStrongestTrackedBeacon);
				}

				newStrongestTrackedBeacon.activeCount++;

				Long roomId = daoCacheService.getTrackerById( newStrongestTrackedBeacon.trackerId ).getRoomId();
				_activeRoomByBeaconId.put( newStrongestTrackedBeacon.beaconId, roomId );
				_strongestBeaconByBeaconIdMap.put( newStrongestTrackedBeacon.beaconId, newStrongestTrackedBeacon );
			}
		}

		usedTrackedBeacons = getSortedUsedTrackedBeacons( _usedTrackedBeacons );
		activeRooms = getSortedActiveRooms( _activeRoomByBeaconId );
		activeRoomsByBeaconId = _activeRoomByBeaconId;
		strongestBeaconByBeaconIdMap = _strongestBeaconByBeaconIdMap;
	}

	private TrackedBeacon checkLastActiveTrackerPriorised( boolean lastStillTracked, TrackedBeacon lastStrongestTrackedBeacon, TrackedBeacon newStrongestTrackedBeacon )
	{
		if( lastStrongestTrackedBeacon != null )
		{
			// active tracker is the same. keep old values and reset last "losing" tracker
			if( lastStrongestTrackedBeacon == newStrongestTrackedBeacon )
			{
				newStrongestTrackedBeacon.fallbackCount = 0;
				newStrongestTrackedBeacon.priorityCheckCount = 0;
				newStrongestTrackedBeacon.priorityCheckTrackedBeaconId = null;
			}
			// active tracker is different
			else
			{
				if( lastStillTracked )
				{
					// use "trackedBeacon" if "lastActiveTracker" has higher priority then "activeTracker"
					if( isLastActiveTrackerPriorised( lastStrongestTrackedBeacon, newStrongestTrackedBeacon) )
					{
						lastStrongestTrackedBeacon.fallbackCount = 0;
						lastStrongestTrackedBeacon.trackPriorityCheck( newStrongestTrackedBeacon);
						newStrongestTrackedBeacon = lastStrongestTrackedBeacon;
					}
				}
				else
				{
					// fallback for a temporary missing trackedBeacon
					if( isLastActiveTrackerPriorisedAsFallback( lastStrongestTrackedBeacon, newStrongestTrackedBeacon ) )
					{
						lastStrongestTrackedBeacon.fallbackCount++;
						lastStrongestTrackedBeacon.trackPriorityCheck( newStrongestTrackedBeacon);
						newStrongestTrackedBeacon = lastStrongestTrackedBeacon;
					}
				}
			}

			// "lastStrongestTrackedBeacon" is not "active" anymore
			if( newStrongestTrackedBeacon != lastStrongestTrackedBeacon )
			{
				lastStrongestTrackedBeacon.fallbackCount = 0;
				lastStrongestTrackedBeacon.priorityCheckCount = 0;
				lastStrongestTrackedBeacon.priorityCheckTrackedBeaconId = null;
				lastStrongestTrackedBeacon.activeCount = 0;
			}
		}

		return newStrongestTrackedBeacon;
	}

	private TrackedBeacon checkLastActiveTrackerPriorisedAsFallback(TrackedBeacon lastStrongestTrackedBeacon)
	{
		// fallback for a temporary missing trackedBeacon
		if( isLastActiveTrackerPriorisedAsFallback( lastStrongestTrackedBeacon ) )
		{
			lastStrongestTrackedBeacon.fallbackCount++;
			lastStrongestTrackedBeacon.priorityCheckCount = 0;
			lastStrongestTrackedBeacon.priorityCheckTrackedBeaconId = null;
			return lastStrongestTrackedBeacon;
		}
		else
		{
			lastStrongestTrackedBeacon.fallbackCount = 0;
			lastStrongestTrackedBeacon.priorityCheckCount = 0;
			lastStrongestTrackedBeacon.priorityCheckTrackedBeaconId = null;
			lastStrongestTrackedBeacon.activeCount = 0;
		}

		return null;
	}

	private boolean isLastActiveTrackerPriorisedAsFallback( TrackedBeacon lastStrongestTrackedBeacon )
	{
		if( lastStrongestTrackedBeacon.activeCount >= CacheServiceBuilderJob.ACTIVE_COUNT_THRESHOLD
			&&
			lastStrongestTrackedBeacon.fallbackCount < CacheServiceBuilderJob.MAX_FALLBACK_COUNT )
		{
			return true;
		}

		return false;
	}

	private boolean isLastActiveTrackerPriorisedAsFallback( TrackedBeacon lastStrongestTrackedBeacon, TrackedBeacon newStrongestTrackedBeacon )
	{
		if( lastStrongestTrackedBeacon.fallbackCount < CacheServiceBuilderJob.MAX_FALLBACK_COUNT )
		{
			return isLastActiveTrackerPriorised( lastStrongestTrackedBeacon, newStrongestTrackedBeacon );
		}

		return false;
	}

	private boolean isLastActiveTrackerPriorised( TrackedBeacon lastStrongestTrackedBeacon, TrackedBeacon newStrongestTrackedBeacon )
	{
		if( lastStrongestTrackedBeacon.activeCount >= CacheServiceBuilderJob.ACTIVE_COUNT_THRESHOLD )
		{
			// new tracker is trying more then one time
			//if( hadEnoughPriorityCheckTries( newStrongestTrackedBeacon, lastStrongestTrackedBeacon ) )
			//{
			//	return false;
			//}

			// new tracker has a very good signal
			if( isStrongRSSI( newStrongestTrackedBeacon, CacheServiceBuilderJob.FORCE_NORMAL_CHECK_RSSI_THRESHOLD ) )
			{
				return false;
			}

			//if( currentActiveTracker.samples > CacheWatcherService.MIN_SAMPLE_THRESHOLD )
			// priorised trackers signal is higher then "reduced" new signal
			if( lastStrongestTrackedBeacon.rssi > (newStrongestTrackedBeacon.rssi - CacheServiceBuilderJob.FORCE_PRIORITY_CHECK_RSSI_THRESHOLD) )
			{
				return true;
			}
		}
		return false;
	}

	private TrackedBeacon getStrongestTrackedBeacon(TrackedBeacon t1, TrackedBeacon t2)
    {
        /*double length1 = LocationHelper.getDistance(  t1.getRssi(), t1.getTxPower() );
        double cmp1 = (100 - length1) * t1.getSamples();

        double length2 = LocationHelper.getDistance(  t2.getRssi(), t2.getTxPower() );
        double cmp2 = (100 - length2) * t2.getSamples();

        if( cmp1 > cmp2 ) return -1;
        if( cmp1 < cmp2 ) return 1;
        return 0;*/

        if( t2 == null )
        {
            return t1;
        }

        int signalStrengh1 = 100 + t1.rssi;
        int priority1 = signalStrengh1 * t1.samples;

        int signalStrengh2 = 100 + t2.rssi;
        int priority2 = signalStrengh2 * t2.samples;

        if( priority1 > priority2 )
            return t1;
        if( priority1 < priority2 )
            return t2;
        return t1;
    }

	private PrepareResult prepareTrackedBeacons(TrackedBeacon lastStrongestTrackedBeacon, Map<Long, List<TrackedBeacon>> trackedBeaconsByBeaconId,
		List<TrackedBeacon> usedTrackedBeacons )
	{
		boolean lastStillTracked = false;

		TrackedBeacon newStrongestTrackedBeacon = null;

		List<TrackedBeacon> trackedBeaconDTOs = trackedBeaconsByBeaconId.get( lastStrongestTrackedBeacon.beaconId );
		if( trackedBeaconDTOs != null )
		{
			List<Long> closeRoomIds = null;
			if( lastStrongestTrackedBeacon != null )
			{
				Long lastRoomId = daoCacheService.getRoomIdByTrackerId(lastStrongestTrackedBeacon.trackerId);
				closeRoomIds = daoCacheService.getCloseRoomIds(lastRoomId);
			}

			for( TrackedBeacon trackedBeaconDTO : trackedBeaconDTOs )
			{
				boolean allowed = true;
				if( closeRoomIds != null )
				{
					Long newRoomId = daoCacheService.getRoomIdByTrackerId( trackedBeaconDTO.trackerId );
					allowed = closeRoomIds.contains( newRoomId ) || isStrongRSSI( trackedBeaconDTO, CacheServiceBuilderJob.SKIP_CLOSE_ROOM_CHECK_RSSI_THRESHOLD );
				}

				if( allowed )
				{
					if( lastStrongestTrackedBeacon != null && lastStrongestTrackedBeacon.trackerId.equals(trackedBeaconDTO.trackerId) )
					{
						trackedBeaconDTO.activeCount = lastStrongestTrackedBeacon.activeCount;
						trackedBeaconDTO.fallbackCount = lastStrongestTrackedBeacon.fallbackCount;
						trackedBeaconDTO.priorityCheckCount = lastStrongestTrackedBeacon.priorityCheckCount;
						trackedBeaconDTO.priorityCheckTrackedBeaconId = lastStrongestTrackedBeacon.priorityCheckTrackedBeaconId;

						lastStrongestTrackedBeacon = trackedBeaconDTO;
						lastStillTracked = true;
					}
					newStrongestTrackedBeacon = getStrongestTrackedBeacon(trackedBeaconDTO, newStrongestTrackedBeacon);
				}
				else
				{
					trackedBeaconDTO.tooFarAway = true;
				}

				usedTrackedBeacons.add( trackedBeaconDTO );
			}
		}

		PrepareResult result = new PrepareResult();
		result.lastStillTracked = lastStillTracked;
		result.lastStrongestTrackedBeacon = lastStrongestTrackedBeacon;
		result.newStrongestTrackedBeacon = newStrongestTrackedBeacon;

		return result;
	}

	private boolean hadEnoughPriorityCheckTries( TrackedBeacon newStrongestTrackedBeacon, TrackedBeacon lastStrongestTrackedBeacon )
	{
		if( newStrongestTrackedBeacon.trackerId.equals( lastStrongestTrackedBeacon.priorityCheckTrackedBeaconId) )
		{
			// new tracker is trying more then one time
			if( lastStrongestTrackedBeacon.priorityCheckCount >= CacheServiceBuilderJob.FORCE_NORMAL_CHECK_ATTEMPT_THRESHOLD )
			{
				return true;
			}
		}

		return false;
	}

	private boolean isStrongRSSI( TrackedBeacon trackedBeacon, int rssi )
	{
		if( trackedBeacon.samples > CacheServiceBuilderJob.MIN_SAMPLE_THRESHOLD )
		{
			return trackedBeacon.rssi > rssi;
		}
		return false;
	}

	private List<TrackedBeacon> getSortedUsedTrackedBeacons(List<TrackedBeacon> usedTrackedBeacons)
	{
		// sort tracked beacons by tracker and beacon id
		usedTrackedBeacons.sort((t1, t2) ->
		{
			if( t1.trackerId < t2.trackerId ) return -1;
			if( t1.trackerId > t2.trackerId ) return 1;

			if( t1.beaconId < t2.beaconId ) return -1;
			if( t1.beaconId > t2.beaconId ) return 1;

			return 0;
		});

		return usedTrackedBeacons;
	}

	private List<Long> getSortedActiveRooms(Map<Long, Long> activeRoomByBeaconId)
	{
		// filter out duplicate rooms and sort them by room id
		List<Long> _activeRooms = new ArrayList<> ( new HashSet<>( activeRoomByBeaconId.values() ) );
		_activeRooms.sort((r1, r2) ->
		{
			if( r1 < r2 ) return -1;
			if( r1 > r2 ) return 1;
			return 0;
		});
		return _activeRooms;
	}

	private Map<Long,List<TrackedBeacon>> getTrackedBeaconsByBeaconIdMap()
	{
		Map<Long, List<TrackedBeacon>> trackedBeaconsByBeaconId = new HashMap<>();
		List<List<TrackedBeacon>> allTrackedBeacons = new ArrayList<>(trackedBeaconsByTrackerIdMap.values());
		for( List<TrackedBeacon> beacons : allTrackedBeacons )
		{
			for( TrackedBeacon beacon : beacons )
			{
				List<TrackedBeacon> trackedBeacons = trackedBeaconsByBeaconId.get( beacon.beaconId );
				if( trackedBeacons == null )
				{
					trackedBeacons = new ArrayList<>();
					trackedBeaconsByBeaconId.put( beacon.beaconId, trackedBeacons );
				}
				trackedBeacons.add(beacon);
			}
		}
		return trackedBeaconsByBeaconId;
	}
}