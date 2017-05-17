package com.holgerhees.indoorpos.frontend.service;

import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.indoorpos.util.LocationHelper;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.*;

@Component( "cacheService" )
public class CacheService
{
	private static Log LOGGER = LogFactory.getLog(CacheService.class);
	private static DecimalFormat df = new DecimalFormat( "#.#" );

	private long lastUpdate;
	Map<Long, List<TrackedBeacon>> trackedBeaconsByTrackerIdMap = new HashMap<>();

	List<Long> activeRooms = new ArrayList<>();
	Map<Long, Long> activeRoomsByBeaconId = new HashMap<>();
	Map<Long, TrackedBeacon> strongestBeaconByBeaconIdMap = new HashMap<>();
	List<TrackedBeacon> usedTrackedBeacons = new ArrayList<>();
	List<BeaconPosition> beaconPositions = new ArrayList<>();

	private Map<Long, TrackedState> lastTrackedStates = new HashMap<>();

	@Autowired
	DAOCacheService daoCacheService;

	public enum State
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

	public static class BeaconPosition
	{
		int x;
		int y;
		Long beaconId;
		Long roomId;

		public int getX()
		{
			return x;
		}

		public int getY()
		{
			return y;
		}

		public Long getBeaconId()
		{
			return beaconId;
		}

		public Long getRoomId()
		{
			return roomId;
		}
	}

	private class TrackedState
	{
		double rssi;
		long timestamp;
	}

	public static class TrackedBeacon
	{
		private Long trackerId;
		private Long beaconId;
		private int txPower;
		private double rssi;
		private double variance;
		private double deviation;
		private int samples;

		// cache service state variables
		private TrackerDTO tracker;
		private BeaconDTO beacon;

		private double adjustedRssi;

		private double adjustedVariance;

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

		public double getRssi()
		{
			return rssi;
		}

		public void setRssi(double rssi)
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

		public double getAdjustedRssi()
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

	public List<BeaconPosition> getBeaconPositions()
	{
		return beaconPositions;
	}

	public void storeTrackerList(Long trackerId, List<TrackedBeacon> trackedBeacons)
	{
		trackedBeaconsByTrackerIdMap.put(trackerId, trackedBeacons);
		lastUpdate = System.currentTimeMillis();
	}

	public void updateActiveTracker()
	{
		List<BeaconDTO> beaconDTOs = daoCacheService.getBeacons();

		List<BeaconPosition> _beaconPositions = new ArrayList<>();
		List<TrackedBeacon> _usedTrackedBeacons = new ArrayList<>();
		Map<Long, TrackedBeacon> _strongestBeaconByBeaconIdMap = new HashMap<>();
		Map<Long, Long> _activeRoomByBeaconId = new HashMap<>();

		Map<Long, List<TrackedBeacon>> trackedBeaconsByBeaconId = getTrackedBeaconsByBeaconIdMap();

		for( BeaconDTO beaconDTO : beaconDTOs )
		{
			// STEP 1: get last active tracker
			TrackedBeacon lastStrongestTrackedBeacon = strongestBeaconByBeaconIdMap.get(beaconDTO.getId());
			TrackedBeacon newStrongestTrackedBeacon = null;

			// STEP 2: get current tracked beacons
			List<TrackedBeacon> trackedBeaconDTOs = trackedBeaconsByBeaconId.get( beaconDTO.getId() );

			// STEP 3: check if lastStrongestTrackedBeacon is still valid
			if( lastStrongestTrackedBeacon != null && ( trackedBeaconDTOs == null || !trackedBeaconDTOs.contains( lastStrongestTrackedBeacon ) ) )
			{
				lastStrongestTrackedBeacon = checkLastActiveTrackerCanFallback( lastStrongestTrackedBeacon );
			}

			// STEP 4: apply history data, update lastStrongestTrackedBeacon and calculate newStrongestTrackedBeacon
			if( trackedBeaconDTOs != null )
			{
				List<Long> closeRoomIds = getCloseRoomIds( lastStrongestTrackedBeacon );

				for( TrackedBeacon trackedBeacon : trackedBeaconDTOs )
				{
					// Cache tracker and beacon. Is used later several times
					trackedBeacon.tracker = daoCacheService.getTrackerById( trackedBeacon.trackerId );
					trackedBeacon.beacon = beaconDTO;

					applyStateData( trackedBeacon);

					LOGGER.info( "TBR: rssi: " + df.format( trackedBeacon.adjustedRssi ) + " (" + df.format( trackedBeacon.rssi ) + "), variance: " + df.format( trackedBeacon.adjustedVariance ) + " (" + df.format( trackedBeacon.variance ) + "), samples: " + trackedBeacon.samples + " (" + trackedBeacon.tracker.getName() + ")" );

					lastStrongestTrackedBeacon = findAndUpdateLastStrongestTrackedBeacon( lastStrongestTrackedBeacon, trackedBeacon );

					newStrongestTrackedBeacon = getStrongestTrackedBeacon( trackedBeacon, newStrongestTrackedBeacon, closeRoomIds );
				}
			}

			// STEP 5: check if lastStrongestTrackedBeacon has higher priority then the "different" newStrongestTrackedBeacon
			newStrongestTrackedBeacon = checkLastActiveTrackerPriorised( lastStrongestTrackedBeacon, newStrongestTrackedBeacon );

			// STEP 6: finalize
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

				_activeRoomByBeaconId.put( newStrongestTrackedBeacon.beaconId, newStrongestTrackedBeacon.tracker.getRoomId() );
				_strongestBeaconByBeaconIdMap.put( newStrongestTrackedBeacon.beaconId, newStrongestTrackedBeacon );
			}
			else if( activeRoomsByBeaconId.containsKey( beaconDTO.getId() ) )
			{
				_activeRoomByBeaconId.put( beaconDTO.getId(), activeRoomsByBeaconId.get( beaconDTO.getId() ) );
			}

			// STEP 7: calculate beacon positions ans collect "used" trackedBeacons
			if( trackedBeaconDTOs != null )
			{
				if( newStrongestTrackedBeacon != null )
				{
					BeaconPosition position = getBeaconPositions( newStrongestTrackedBeacon, trackedBeaconDTOs );
					_beaconPositions.add( position );
				}

				_usedTrackedBeacons.addAll( trackedBeaconDTOs );
			}
		}

		activeRooms = getSortedActiveRooms( _activeRoomByBeaconId );
		activeRoomsByBeaconId = _activeRoomByBeaconId;
		strongestBeaconByBeaconIdMap = _strongestBeaconByBeaconIdMap;
		usedTrackedBeacons = getSortedUsedTrackedBeacons( _usedTrackedBeacons );
		beaconPositions = _beaconPositions;
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

			if( newStrongestTrackedBeacon != null && newStrongestTrackedBeacon != lastStrongestTrackedBeacon )
			{
				// new tracker has a very good signal
				if( isStrongRSSI( newStrongestTrackedBeacon ) )
				{
					return newStrongestTrackedBeacon;
				}

				if( isLastActiveTrackerRSSIPriorised( lastStrongestTrackedBeacon, newStrongestTrackedBeacon ) )
				{
					lastStrongestTrackedBeacon.states.add( State.PRIORITY_SIGNAL );
					return lastStrongestTrackedBeacon;
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
		return trackedBeacon.adjustedRssi >= trackedBeacon.tracker.getStrongSignalRssiThreshold() - trackedBeacon.beacon.getRssiOffset();
	}

	private boolean isLastActiveTrackerRSSIPriorised(TrackedBeacon lastStrongestTrackedBeacon, TrackedBeacon newStrongestTrackedBeacon)
	{
		// priorised trackers signal is higher then "reduced" new signal
		return lastStrongestTrackedBeacon.adjustedRssi > (newStrongestTrackedBeacon.adjustedRssi - lastStrongestTrackedBeacon.tracker.getPriorisedRssiOffset() );
	}

	private TrackedBeacon getStrongestTrackedBeacon(TrackedBeacon t1, TrackedBeacon t2)
	{
		if( t2 == null ) return t1;
		if( t1.adjustedRssi > t2.adjustedRssi ) return t1;
		if( t1.adjustedRssi < t2.adjustedRssi ) return t2;
		return t1;
	}

	private TrackedBeacon getStrongestTrackedBeacon( TrackedBeacon trackedBeacon, TrackedBeacon newStrongestTrackedBeacon, List<Long> closeRoomIds )
	{
		boolean hasStrongSignal = true;
		boolean hasMinSignal = true;
		boolean isCloseRoom = true;

		if( trackedBeacon.activeCount == 0 )
		{
			hasStrongSignal = isStrongRSSI( trackedBeacon );
			if( hasStrongSignal ) trackedBeacon.states.add( State.STRONG_SIGNAL );

			hasMinSignal = trackedBeacon.adjustedRssi >= trackedBeacon.tracker.getMinRssi() - trackedBeacon.beacon.getRssiOffset();
			if( !hasMinSignal ) trackedBeacon.states.add( State.MIN_RSSI );

			isCloseRoom = closeRoomIds == null || closeRoomIds.contains( trackedBeacon.tracker.getRoomId() );
			if( !isCloseRoom ) trackedBeacon.states.add( State.TOO_FAR_AWAY );
		}

		// check if it is a "close" room or has a strong signal
		if( ( hasStrongSignal || hasMinSignal ) && isCloseRoom )
		{
			newStrongestTrackedBeacon = getStrongestTrackedBeacon(trackedBeacon, newStrongestTrackedBeacon);
		}
		else
		{
			trackedBeacon.states.add( State.SKIPPED );
		}

		return newStrongestTrackedBeacon;
	}

	private TrackedBeacon findAndUpdateLastStrongestTrackedBeacon( TrackedBeacon lastStrongestTrackedBeacon, TrackedBeacon trackedBeacon )
	{
		// update lastStrongestTrackedBeacon reference with a new trackedBeacon object
		if( lastStrongestTrackedBeacon != null && lastStrongestTrackedBeacon.trackerId.equals(trackedBeacon.trackerId) )
		{
			trackedBeacon.activeCount = lastStrongestTrackedBeacon.activeCount;
			trackedBeacon.fallbackCount = 0;
			lastStrongestTrackedBeacon = trackedBeacon;
		}

		return lastStrongestTrackedBeacon;
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

	private List<Long> getCloseRoomIds(TrackedBeacon lastStrongestTrackedBeacon)
	{
		if( lastStrongestTrackedBeacon != null )
		{
			Long lastRoomId = daoCacheService.getTrackerById( lastStrongestTrackedBeacon.trackerId ).getRoomId();
			return daoCacheService.getCloseRoomIds(lastRoomId);
		}

		return null;
	}

	private void applyStateData( TrackedBeacon trackedBeacon)
	{
		long now = System.currentTimeMillis();

		Long key = ( trackedBeacon.trackerId * 100000 ) + trackedBeacon.beaconId;

		TrackedState state = lastTrackedStates.get( key);
		if( state == null )
		{
			state = new TrackedState();
			state.rssi = -100.0;
			lastTrackedStates.put( key, state );
		}
		else if( now - state.timestamp > CacheServiceBuilderJob.INTERVAL_LENGTH * CacheServiceBuilderJob.OUTDATED_COUNT )
		{
			state.rssi = -100.0;
		}

		state.timestamp = now;

		/*// STEP 1: use number of samples as a percentage how much we should go in this direction
		//         factor is between 0 and 1
		int maxSamples = CacheServiceBuilderJob.INTERVAL_LENGTH / CacheServiceBuilderJob.FREQUENCY;
		int factor = trackedBeacon.samples / maxSamples;
		if( factor < 0 ) factor = 0;
		if( factor > 1 ) factor = 1;

		//return (int) ( ( variance2 * rssi1 + variance1 * rssi2 ) / ( rssi1 + rssi2 ) );

		// STEP 2: calculate difference between old and new RSSI
		double totalDiffRssi = trackedBeacon.rssi - state.rssi;

		// STEP 3: calculate effectiveDiffRssi
		double effectiveDiffRssi = totalDiffRssi * factor;

		state.rssi += effectiveDiffRssi;

		trackedBeacon.adjustedRssi = state.rssi;
		//trackedBeacon.adjustedVariance = adjustedVariance;*/

		// STEP 1: increase variance based on missing samples.
		//         if all samples are there we increase the variance by 0
		//         if all samples are missing we increase the variance by 80 (REFERENCE_VARIANCE)
		int maxSamples = CacheServiceBuilderJob.INTERVAL_LENGTH / CacheServiceBuilderJob.FREQUENCY;
		int missingSamples = maxSamples - trackedBeacon.samples;
		double adjustedVariance = trackedBeacon.variance + ( ( missingSamples * CacheServiceBuilderJob.REFERENCE_VARIANCE ) / maxSamples );

		// STEP 2: calculate difference between old and new RSSI
		double totalDiffRssi = trackedBeacon.rssi - state.rssi;

		// STEP 3: calculate percentage of adjusted variance
		//         variance of 80.0 (REFERENCE_VARIANCE) means 0%
		//         variance of 0.0 means 100%
		double percent = adjustedVariance * 100.0 / CacheServiceBuilderJob.REFERENCE_VARIANCE;
		if( percent < 0.0 ) percent = 0.0;
		else if( percent > 100.0 ) percent = 100.0;
		//         reverse
		percent = 100.0 - percent;

		// STEP 4: calculate effectiveDiffRssi
		double effectiveDiffRssi = totalDiffRssi * percent / 100.0;

		state.rssi += effectiveDiffRssi;

		trackedBeacon.adjustedRssi = state.rssi;
		trackedBeacon.adjustedVariance = adjustedVariance;
	}

	private BeaconPosition getBeaconPositions(TrackedBeacon newStrongestTrackedBeacon, List<TrackedBeacon> trackedBeaconDTOs )
	{
		double[] centroid;

		if( trackedBeaconDTOs.size() == 1 )
		{
			centroid = new double[]{ newStrongestTrackedBeacon.tracker.getPosX(), newStrongestTrackedBeacon.tracker.getPosY() };
		}
		else
		{
			double[][] positions = new double[trackedBeaconDTOs.size()][2];
			double[] distances = new double[trackedBeaconDTOs.size()];

			for( int i = 0; i < trackedBeaconDTOs.size(); i++ )
			{
				TrackedBeacon trackedBeacon = trackedBeaconDTOs.get( i );
				positions[i][0] = trackedBeacon.tracker.getPosX();
				positions[i][1] = trackedBeacon.tracker.getPosY();

				double distance = LocationHelper.getDistance( trackedBeacon.rssi, -70 );

				// convert it to pixel
				distances[i] = distance * CacheServiceBuilderJob.MAP_WIDTH / 10;
			}

			NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver( new TrilaterationFunction( positions, distances ), new LevenbergMarquardtOptimizer() );
			LeastSquaresOptimizer.Optimum optimum = solver.solve();

			// the answer
			centroid = optimum.getPoint().toArray();

			if( centroid[0] > CacheServiceBuilderJob.MAP_WIDTH ) centroid[0] = CacheServiceBuilderJob.MAP_WIDTH;
			if( centroid[1] > CacheServiceBuilderJob.MAP_HEIGHT ) centroid[1] = CacheServiceBuilderJob.MAP_HEIGHT;
		}

		BeaconPosition position = new BeaconPosition();
		position.beaconId = newStrongestTrackedBeacon.beaconId;
		position.roomId = newStrongestTrackedBeacon.tracker.getRoomId();
		position.x = (int) centroid[0];
		position.y = (int) centroid[1];

		return position;
	}

	/*private int updateRssi( int rssi1, double variance1, int rssi2, double variance2 )
	{
		return (int) ( ( variance2 * rssi1 + variance1 * rssi2 ) / ( rssi1 + rssi2 ) );
	}

	private double updateVariance( double variance1, double variance2 )
	{
		return 1 / ( 1 / variance1 + 1 / variance2 );
	}*/
}