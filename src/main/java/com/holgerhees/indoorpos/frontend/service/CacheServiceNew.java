package com.holgerhees.indoorpos.frontend.service;

import com.holgerhees.indoorpos.frontend.websockets.tracker.TrackerWatcher;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.*;

@Component( "cacheServiceNew" )
public class CacheServiceNew
{
	private static Log LOGGER = LogFactory.getLog( CacheServiceNew.class );
	private static DecimalFormat df = new DecimalFormat( "#.##" );

	@Autowired
	DAOCacheService daoCacheService;

	public static enum State
	{
		ACTIVE( "active" ),
		PRIORITY( "priority" ),
		FALLBACK( "fallback" ),
		SKIPPED( "skipped" ),

		MIN_RSSI( "min_rssi" ),
		PRIORITY_SIGNAL( "priority_signal" ),
		STRONG_SIGNAL( "strong_signal" ),
		TOO_FAR_AWAY( "too_far_away" );

		private String id;

		State( String id )
		{
			this.id = id;
		}

		public String toString()
		{
			return id;
		}
	}

	private Map<Long, List<TrackedBeacon>> trackedBeaconsByTrackerId = new HashMap<>();
	private Map<String, Position> lastTrackedPositions = new HashMap<>();

	private List<Long> activeRooms = new ArrayList<>();
	private List<TrackedBeacon> usedTrackedBeacons = new ArrayList<>();

	public static class TrackedBeacon
	{
		private Long trackerId;
		private Long beaconId;
		private int rssi;
		double variance;
		double deviation;
		int samples;

		private List<State> states = new ArrayList<>();

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

		public int getRssi()
		{
			return rssi;
		}

		public void setRssi( int rssi )
		{
			this.rssi = rssi;
		}

		public double getVariance()
		{
			return variance;
		}

		public void setVariance( double variance )
		{
			this.variance = variance;
		}

		public double getDeviation()
		{
			return deviation;
		}

		public void setDeviation( double deviation )
		{
			this.deviation = deviation;
		}

		public int getSamples()
		{
			return samples;
		}

		public void setSamples( int samples )
		{
			this.samples = samples;
		}

		public List<State> getStates()
		{
			return states;
		}
	}

	private class Position
	{
		int rssi;
		TrackedBeacon trackedBeacon;
	}

	public void storeTrackerList( Long trackerId, List<TrackedBeacon> trackedBeacons )
	{
		trackedBeaconsByTrackerId.put( trackerId, trackedBeacons );
	}

	public void updateActiveTracker()
	{
		//LOGGER.info( "TRACKED SIZE: " + trackedBeaconsByBeaconId.size() );

		Map<Long, List<TrackedBeacon>> trackedBeaconsByBeaconId = getTrackedBeaconsByBeaconId();

		Set<Long> _activeRooms = new HashSet<>();
		List<TrackedBeacon> _usedTrackedBeacons = new ArrayList<>();

		List<BeaconDTO> beaconDTOs = daoCacheService.getBeacons();
		for( BeaconDTO beaconDTO : beaconDTOs )
		{
			List<TrackedBeacon> trackedBeacons = trackedBeaconsByBeaconId.get( beaconDTO.getId() );
			if( trackedBeacons == null )
			{
				continue;
			}

			adjustPositions( trackedBeacons );

			TrackedBeacon strongestTrackedBeacon = null;
			for( TrackedBeacon trackedBeacon: trackedBeacons )
			{
				String key = trackedBeacon.trackerId + "|" + trackedBeacon.beaconId;

				Position position = lastTrackedPositions.get( key );

				if( strongestTrackedBeacon == null || strongestTrackedBeacon.rssi < position.rssi ) strongestTrackedBeacon = position.trackedBeacon;

				_usedTrackedBeacons.add( trackedBeacon );
			}

			strongestTrackedBeacon.states.add( State.ACTIVE );

			TrackerDTO trackerDTO = daoCacheService.getTrackerById( strongestTrackedBeacon.trackerId );

			_activeRooms.add( trackerDTO.getRoomId() );
		}

		this.activeRooms = new ArrayList( _activeRooms );
		this.usedTrackedBeacons = _usedTrackedBeacons;
	}

	private void adjustPositions( List<TrackedBeacon> trackedBeacons )
	{
		for( TrackedBeacon trackedBeacon : trackedBeacons )
		{
			String key = trackedBeacon.trackerId + "|" + trackedBeacon.beaconId;

			Position position = lastTrackedPositions.get( key );
			if( position == null )
			{
				position = new Position();
				position.rssi = trackedBeacon.rssi;
				position.trackedBeacon = trackedBeacon;
				lastTrackedPositions.put( key, position );
			}
			else
			{
				int maxSamples = CacheServiceBuilderJob.INTERVAL_LENGTH / CacheServiceBuilderJob.FREQUENCY;
				int missingSamples = maxSamples - trackedBeacon.samples;

				double adjustedVariance = trackedBeacon.variance + ( ( missingSamples * 80.0 ) / maxSamples );
				double diffRssi = trackedBeacon.rssi - position.rssi;

				double reverseVariance = 80 - adjustedVariance;
				if( reverseVariance < 0 ) reverseVariance = 0;
				else if( reverseVariance > 80 ) reverseVariance = 80;

				double effectiveDiffRssi = reverseVariance * diffRssi / 80;

				double oldRssi = position.rssi;

				position.rssi += effectiveDiffRssi;

				TrackerDTO trackerDTO = daoCacheService.getTrackerById( trackedBeacon.trackerId );

				//LOGGER.info( "TBR: rssi: " + position.rssi + " (" + oldRssi + " => " + trackedBeacon.rssi + "), variance: " + df.format( trackedBeacon.variance ) + " (" + df.format( adjustedVariance ) + "), samples: " + trackedBeacon.samples + " (" + trackerDTO.getName() + ")" );
			}
		}
	}

	private Map<Long, List<TrackedBeacon>> getTrackedBeaconsByBeaconId()
	{
		Map<Long, List<TrackedBeacon>> trackedBeaconsByBeaconId = new HashMap<>();
		List<List<TrackedBeacon>> allTrackedBeacons = new ArrayList<>( trackedBeaconsByTrackerId.values() );
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
				trackedBeacons.add( beacon );
			}
		}
		return trackedBeaconsByBeaconId;
	}

	private int updateRssi( int rssi1, double variance1, int rssi2, double variance2 )
	{
		return (int) ( ( variance2 * rssi1 + variance1 * rssi2 ) / ( rssi1 + rssi2 ) );
	}

	private double updateVariance( double variance1, double variance2 )
	{
		return 1 / ( 1 / variance1 + 1 / variance2 );
	}

	public List<Long> getActiveRooms()
	{
		return activeRooms;
	}

	public List<TrackedBeacon> getUsedTrackedBeacons()
	{
		return usedTrackedBeacons;
	}
}