package com.holgerhees.indoorpos.frontend.service;

import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.*;

@Component( "cacheService" )
public class CacheService
{
	private static Log LOGGER = LogFactory.getLog(CacheService.class);
	private static DecimalFormat df = new DecimalFormat( "#.##" );

	private long lastUpdate;
	Map<Long, List<TrackedBeacon>> trackedBeaconsByTrackerIdMap = new HashMap<>();

	List<Long> activeRooms = new ArrayList<>();
	Map<Long, Long> activeRoomsByBeaconId = new HashMap<>();
	Map<Long, TrackedBeacon> strongestBeaconByBeaconIdMap = new HashMap<>();
	List<TrackedBeacon> usedTrackedBeacons = new ArrayList<>();

	private Map<String, Position> lastTrackedPositions = new HashMap<>();

	@Autowired
	DAOCacheService daoCacheService;

	private class Position
	{
		int rssi;
		long timestamp;
	}

	private class PrepareResult
	{
		TrackedBeacon newStrongestTrackedBeacon;
		TrackedBeacon lastStrongestTrackedBeacon;
		boolean lastStillTracked;
	}

	public static enum State
	{
		ACTIVE("active"),
		PRIORITY("priority"),
		FALLBACK("fallback"),
		SKIPPED("skipped"),

		MIN_RSSI( "min_rssi"),
		PRIORITY_SIGNAL("priority_signal"),
		STRONG_SIGNAL("strong_signal"),
		TOO_FAR_AWAY("too_far_away");

		private String id;

		State(String id)
		{
			this.id = id;
		}

		public String toString()
		{
			return id;
		}
	}

	public static class TrackedBeacon
	{
		private Long trackerId;
		private Long beaconId;
		private int txPower;
		private int rssi;
		private double variance;
		private double deviation;
		private int samples;

		private int adjustedRssi;

		// cache service state variables
		private int activeCount = 0;
		private int fallbackCount = 0;
		private List<State> states = new ArrayList<>();

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

		public double getVariance()
		{
			return variance;
		}

		public void setVariance(double variance)
		{
			this.variance = variance;
		}

		public void setDeviation( double deviation )
		{
			this.deviation = deviation;
		}

		public int getAdjustedRssi()
		{
			return adjustedRssi;
		}

		public List<State> getStates()
		{
			return this.states;
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

			List<TrackedBeacon> trackedBeaconDTOs = trackedBeaconsByBeaconId.get( beaconDTO.getId() );

			if( trackedBeaconDTOs != null ) adjustPositions( trackedBeaconDTOs );

			// STEP 1
			// detect NEW STRONGEST TRACKED BEACON (incl. possible rooms check)
			// update LAST STRONGEST TRACKED BEACON
			// check if LAST STRONGEST TRACKED BEACON is still tracked
			PrepareResult _result = getRelevantTrackedBeacons( beaconDTO, lastStrongestTrackedBeacon, trackedBeaconDTOs, _usedTrackedBeacons);
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
				if( lastStrongestTrackedBeacon != null )
				{
					if( lastStrongestTrackedBeacon == newStrongestTrackedBeacon && !lastStillTracked ) _usedTrackedBeacons.add(lastStrongestTrackedBeacon);
					if( isPriorised( lastStrongestTrackedBeacon ) ) lastStrongestTrackedBeacon.states.add( State.PRIORITY );
				}
				newStrongestTrackedBeacon.states.add( State.ACTIVE );
				newStrongestTrackedBeacon.activeCount++;

				TrackerDTO trackerDTO = daoCacheService.getTrackerById( newStrongestTrackedBeacon.trackerId );

				//LOGGER.info( "TBR: rssi: " + newStrongestTrackedBeacon.rssi + " (" + newStrongestTrackedBeacon.adjustedRssi + ") - (" + trackerDTO.getName() + ")" );

				Long roomId = trackerDTO.getRoomId();
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
						newStrongestTrackedBeacon = lastStrongestTrackedBeacon;
					}
				}
				else
				{
					// fallback for a temporary missing trackedBeacon
					if( isLastActiveTrackerPriorisedAsFallback( lastStrongestTrackedBeacon, newStrongestTrackedBeacon ) )
					{
						lastStrongestTrackedBeacon.states.add( State.FALLBACK);
						lastStrongestTrackedBeacon.fallbackCount++;
						newStrongestTrackedBeacon = lastStrongestTrackedBeacon;
					}
				}
			}

			// "lastStrongestTrackedBeacon" is not "active" anymore
			if( newStrongestTrackedBeacon != lastStrongestTrackedBeacon )
			{
				lastStrongestTrackedBeacon.fallbackCount = 0;
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
			lastStrongestTrackedBeacon.states.add( State.FALLBACK);
			lastStrongestTrackedBeacon.fallbackCount++;
			return lastStrongestTrackedBeacon;
		}
		else
		{
			lastStrongestTrackedBeacon.fallbackCount = 0;
			lastStrongestTrackedBeacon.activeCount = 0;
		}

		return null;
	}

	private boolean isLastActiveTrackerPriorisedAsFallback( TrackedBeacon lastStrongestTrackedBeacon )
	{
		if( isPriorised( lastStrongestTrackedBeacon ) && canFallback( lastStrongestTrackedBeacon ) ) return true;

		return false;
	}

	private boolean isLastActiveTrackerPriorisedAsFallback( TrackedBeacon lastStrongestTrackedBeacon, TrackedBeacon newStrongestTrackedBeacon )
	{
		if( canFallback( lastStrongestTrackedBeacon ) ) return isLastActiveTrackerPriorised( lastStrongestTrackedBeacon, newStrongestTrackedBeacon );

		return false;
	}

	private boolean isLastActiveTrackerPriorised( TrackedBeacon lastStrongestTrackedBeacon, TrackedBeacon newStrongestTrackedBeacon )
	{
		if( isPriorised( lastStrongestTrackedBeacon ) )
		{
			// new tracker has a very good signal
			if( isStrongRSSI( newStrongestTrackedBeacon ) )
			{
				return false;
			}

			if( isLastActiveTrackerRSSIPriorised( lastStrongestTrackedBeacon, newStrongestTrackedBeacon ) )
			{
				return true;
			}
		}

		return false;
	}

	private boolean canFallback( TrackedBeacon trackedBeacon )
	{
		return  trackedBeacon.fallbackCount < CacheServiceBuilderJob.MAX_FALLBACK_COUNT;
	}

	private boolean isPriorised( TrackedBeacon trackedBeacon )
	{
		return trackedBeacon.activeCount >= CacheServiceBuilderJob.ACTIVE_COUNT_THRESHOLD;
	}

	private boolean isStrongRSSI( TrackedBeacon trackedBeacon )
	{
		return trackedBeacon.adjustedRssi >= daoCacheService.getTrackerById( trackedBeacon.trackerId ).getStrongSignalRssiThreshold();
	}

	private boolean isLastActiveTrackerRSSIPriorised(TrackedBeacon lastStrongestTrackedBeacon, TrackedBeacon newStrongestTrackedBeacon)
	{
		// priorised trackers signal is higher then "reduced" new signal
		if( lastStrongestTrackedBeacon.adjustedRssi > (newStrongestTrackedBeacon.adjustedRssi - daoCacheService.getTrackerById( lastStrongestTrackedBeacon.trackerId ).getPriorisedRssiOffset() ) )
		{
			lastStrongestTrackedBeacon.states.add( State.PRIORITY_SIGNAL );
			return true;
		}

		return false;
	}

	private TrackedBeacon getStrongestTrackedBeacon(TrackedBeacon t1, TrackedBeacon t2)
	{
		if( t2 == null )
		{
			return t1;
		}

		if( t1.adjustedRssi > t2.adjustedRssi )
			return t1;
		if( t1.adjustedRssi < t2.adjustedRssi )
			return t2;
		return t1;
	}

	private boolean isAllowed( TrackerDTO tracker, TrackedBeacon trackedBeaconDTO, List<Long> closeRoomIds )
	{
		boolean allowed = true;
		boolean hasStrongSignal = isStrongRSSI( trackedBeaconDTO );
		if( hasStrongSignal ) trackedBeaconDTO.states.add( State.STRONG_SIGNAL );

		if( closeRoomIds != null )
		{
			Long newRoomId = tracker.getRoomId();
			boolean isCloseRoom = closeRoomIds.contains( newRoomId );
			if( !isCloseRoom ) trackedBeaconDTO.states.add( State.TOO_FAR_AWAY );

			allowed = isCloseRoom || hasStrongSignal;
		}

		if( !allowed ) trackedBeaconDTO.states.add( State.SKIPPED );

		return allowed;
	}

	private PrepareResult getRelevantTrackedBeacons(BeaconDTO beacon, TrackedBeacon lastStrongestTrackedBeacon, List<TrackedBeacon> trackedBeaconDTOs,
		List<TrackedBeacon> usedTrackedBeacons)
	{
		boolean lastStillTracked = false;

		TrackedBeacon newStrongestTrackedBeacon = null;

		if( trackedBeaconDTOs != null )
		{
			List<Long> closeRoomIds = null;
			if( lastStrongestTrackedBeacon != null )
			{
				Long lastRoomId = daoCacheService.getTrackerById( lastStrongestTrackedBeacon.trackerId ).getRoomId();
				closeRoomIds = daoCacheService.getCloseRoomIds(lastRoomId);
			}

			for( TrackedBeacon trackedBeaconDTO : trackedBeaconDTOs )
			{
				TrackerDTO tracker = daoCacheService.getTrackerById( trackedBeaconDTO.trackerId);
				boolean allowed = isAllowed( tracker, trackedBeaconDTO, closeRoomIds );

				if( allowed )
				{
					if( lastStrongestTrackedBeacon != null && lastStrongestTrackedBeacon.trackerId.equals(trackedBeaconDTO.trackerId) )
					{
						trackedBeaconDTO.activeCount = lastStrongestTrackedBeacon.activeCount;
						trackedBeaconDTO.fallbackCount = lastStrongestTrackedBeacon.fallbackCount;
						lastStrongestTrackedBeacon = trackedBeaconDTO;
						lastStillTracked = true;
					}
					if( trackedBeaconDTO.rssi >= tracker.getMinRssi() || trackedBeaconDTO == lastStrongestTrackedBeacon )
					{
						newStrongestTrackedBeacon = getStrongestTrackedBeacon(trackedBeaconDTO, newStrongestTrackedBeacon);
					}
					else
					{
						trackedBeaconDTO.states.add( State.SKIPPED );
						trackedBeaconDTO.states.add( State.MIN_RSSI );
					}
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

	private void adjustPositions( List<TrackedBeacon> trackedBeacons )
	{
		long now = System.currentTimeMillis();

		for( TrackedBeacon trackedBeacon : trackedBeacons )
		{
			String key = trackedBeacon.trackerId + "|" + trackedBeacon.beaconId;

			Position position = lastTrackedPositions.get( key );
			if( position == null )
			{
				position = new Position();
				position.rssi = -100;
				lastTrackedPositions.put( key, position );
				//LOGGER.info( "create" );
			}
			else if( now - position.timestamp > CacheServiceBuilderJob.INTERVAL_LENGTH * 5 )
			{
				position.rssi = -100;
			}

			int maxSamples = CacheServiceBuilderJob.INTERVAL_LENGTH / CacheServiceBuilderJob.FREQUENCY;
			int missingSamples = maxSamples - trackedBeacon.samples;

			double adjustedVariance = trackedBeacon.variance + ( ( missingSamples * 80.0 ) / maxSamples );
			double diffRssi = trackedBeacon.rssi - position.rssi;

			double reverseVariance = 80 - adjustedVariance;
			if( reverseVariance < 0 ) reverseVariance = 0;
			else if( reverseVariance > 80 ) reverseVariance = 80;

			double effectiveDiffRssi = reverseVariance * diffRssi / 80;

			position.rssi += effectiveDiffRssi;
			//LOGGER.info( "update" );
			//TrackerDTO trackerDTO = daoCacheService.getTrackerById( trackedBeacon.trackerId );

			LOGGER.info( "TBR: rssi: " + position.rssi + " (" + trackedBeacon.rssi + "), variance: " + df.format( adjustedVariance ) + " (" + df.format( trackedBeacon.variance ) + "), samples: " + trackedBeacon.samples + " (" + daoCacheService.getTrackerById( trackedBeacon.trackerId ).getName() + ")" );

			position.timestamp = now;

			trackedBeacon.adjustedRssi = position.rssi;
		}
	}
}