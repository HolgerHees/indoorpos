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

		// cache service state variables
		private int adjustedRssi;
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
			TrackedBeacon newStrongestTrackedBeacon = null;

			// get current tracked beacons
			List<TrackedBeacon> trackedBeaconDTOs = trackedBeaconsByBeaconId.get( beaconDTO.getId() );

			// check if lastStrongestTrackedBeacon is still valid
			if( lastStrongestTrackedBeacon != null && ( trackedBeaconDTOs == null || !trackedBeaconDTOs.contains( lastStrongestTrackedBeacon ) ) )
			{
				lastStrongestTrackedBeacon = checkLastActiveTrackerCanFallback( lastStrongestTrackedBeacon );
			}

			// apply history data and calculate new strongest beacon
			if( trackedBeaconDTOs != null )
			{
				applyHistoryData( trackedBeaconDTOs );

				TrackedBeacon[] _result = getRelevantTrackedBeacons( lastStrongestTrackedBeacon, trackedBeaconDTOs );
				lastStrongestTrackedBeacon = _result[0];
				newStrongestTrackedBeacon = _result[1];
			}

			// check if lastStrongestTrackedBeacon has higher priority then the "different" newStrongestTrackedBeacon
			newStrongestTrackedBeacon = checkLastActiveTrackerPriorised( lastStrongestTrackedBeacon, newStrongestTrackedBeacon );

			if( newStrongestTrackedBeacon != null )
			{
				// fallback means it was not found as a tracked beacon. we have to add them.
				if( newStrongestTrackedBeacon.fallbackCount > 0 )
				{
					if( trackedBeaconDTOs == null ) trackedBeaconDTOs = new ArrayList<>();
					trackedBeaconDTOs.add(newStrongestTrackedBeacon);
				}

				newStrongestTrackedBeacon.states.add( State.ACTIVE );

				newStrongestTrackedBeacon.activeCount++;

				TrackerDTO trackerDTO = daoCacheService.getTrackerById( newStrongestTrackedBeacon.trackerId );

				//LOGGER.info( "TBR: rssi: " + newStrongestTrackedBeacon.rssi + " (" + newStrongestTrackedBeacon.adjustedRssi + ") - (" + trackerDTO.getName() + ")" );

				_activeRoomByBeaconId.put( newStrongestTrackedBeacon.beaconId, trackerDTO.getRoomId() );
				_strongestBeaconByBeaconIdMap.put( newStrongestTrackedBeacon.beaconId, newStrongestTrackedBeacon );
			}
			else if( activeRoomsByBeaconId.containsKey( beaconDTO.getId() ) )
			{
				_activeRoomByBeaconId.put( beaconDTO.getId(), activeRoomsByBeaconId.get( beaconDTO.getId() ) );
			}

			if( trackedBeaconDTOs != null ) _usedTrackedBeacons.addAll( trackedBeaconDTOs );
		}

		activeRooms = getSortedActiveRooms( _activeRoomByBeaconId );
		activeRoomsByBeaconId = _activeRoomByBeaconId;
		strongestBeaconByBeaconIdMap = _strongestBeaconByBeaconIdMap;
		usedTrackedBeacons = getSortedUsedTrackedBeacons( _usedTrackedBeacons );
	}

	private TrackedBeacon checkLastActiveTrackerCanFallback( TrackedBeacon lastStrongestTrackedBeacon )
	{

		if( lastStrongestTrackedBeacon.fallbackCount > CacheServiceBuilderJob.MAX_FALLBACK_COUNT )
		{
			return null;
		}
		else
		{
			lastStrongestTrackedBeacon.states.add( State.FALLBACK );
			lastStrongestTrackedBeacon.fallbackCount++;
			return lastStrongestTrackedBeacon;
		}
	}

	private TrackedBeacon checkLastActiveTrackerPriorised( TrackedBeacon lastStrongestTrackedBeacon, TrackedBeacon newStrongestTrackedBeacon )
	{
		if( lastStrongestTrackedBeacon != null && isPriorised( lastStrongestTrackedBeacon ) )
		{
			lastStrongestTrackedBeacon.states.add( State.PRIORITY );

			if( newStrongestTrackedBeacon != null )
			{
				// new tracker has a very good signal
				if( isStrongRSSI( newStrongestTrackedBeacon ) )
				{
					return newStrongestTrackedBeacon;
				}

				if( newStrongestTrackedBeacon != lastStrongestTrackedBeacon )
				{
					if( isLastActiveTrackerRSSIPriorised( lastStrongestTrackedBeacon, newStrongestTrackedBeacon ) )
					{
						lastStrongestTrackedBeacon.states.add( State.PRIORITY_SIGNAL );
						return lastStrongestTrackedBeacon;
					}
				}
			}
			else
			{
				return lastStrongestTrackedBeacon;
			}
		}

		return newStrongestTrackedBeacon;
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
		return lastStrongestTrackedBeacon.adjustedRssi > (newStrongestTrackedBeacon.adjustedRssi - daoCacheService.getTrackerById( lastStrongestTrackedBeacon.trackerId ).getPriorisedRssiOffset() );
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

	private TrackedBeacon[] getRelevantTrackedBeacons( TrackedBeacon lastStrongestTrackedBeacon, List<TrackedBeacon> trackedBeaconDTOs )
	{
		TrackedBeacon newStrongestTrackedBeacon = null;

		List<Long> closeRoomIds = null;
		if( lastStrongestTrackedBeacon != null )
		{
			Long lastRoomId = daoCacheService.getTrackerById( lastStrongestTrackedBeacon.trackerId ).getRoomId();
			closeRoomIds = daoCacheService.getCloseRoomIds(lastRoomId);
		}

		for( TrackedBeacon trackedBeaconDTO : trackedBeaconDTOs )
		{
			// update lastStrongestTrackedBeacon reference with a new trackedBeacon object
			if( lastStrongestTrackedBeacon != null && lastStrongestTrackedBeacon.trackerId.equals(trackedBeaconDTO.trackerId) )
			{
				trackedBeaconDTO.activeCount = lastStrongestTrackedBeacon.activeCount;
				trackedBeaconDTO.fallbackCount = 0;
				lastStrongestTrackedBeacon = trackedBeaconDTO;
			}

			boolean hasStrongSignal = true;
			boolean hasMinSignal = true;
			boolean isCloseRoom = true;

			if( trackedBeaconDTO.activeCount == 0 )
			{
				TrackerDTO tracker = daoCacheService.getTrackerById( trackedBeaconDTO.trackerId);

				hasStrongSignal = isStrongRSSI( trackedBeaconDTO );
				if( hasStrongSignal ) trackedBeaconDTO.states.add( State.STRONG_SIGNAL );

				hasMinSignal = trackedBeaconDTO.rssi >= tracker.getMinRssi();
				if( !hasMinSignal ) trackedBeaconDTO.states.add( State.MIN_RSSI );

				isCloseRoom = closeRoomIds == null || closeRoomIds.contains( tracker.getRoomId() );
				if( !isCloseRoom ) trackedBeaconDTO.states.add( State.TOO_FAR_AWAY );
			}

			// check if it is a "close" room or has a strong signal
			if( ( hasStrongSignal || hasMinSignal ) && isCloseRoom )
			{
				newStrongestTrackedBeacon = getStrongestTrackedBeacon(trackedBeaconDTO, newStrongestTrackedBeacon);
			}
			else
			{
				trackedBeaconDTO.states.add( State.SKIPPED );
			}
		}

		return new TrackedBeacon[]{ lastStrongestTrackedBeacon, newStrongestTrackedBeacon };
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

	private void applyHistoryData( List<TrackedBeacon> trackedBeacons )
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
			else if( now - position.timestamp > CacheServiceBuilderJob.INTERVAL_LENGTH * CacheServiceBuilderJob.OUTDATED_COUNT )
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