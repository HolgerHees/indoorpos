package com.holgerhees.indoorpos.cron;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.holgerhees.indoorpos.persistance.dao.BeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.RoomDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackedBeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.RoomDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.indoorpos.util.LocationHelper;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

@Component
public class RefreshPosition
{
	private static Log LOGGER = LogFactory.getLog(RefreshPosition.class);

	@Autowired
	BeaconDAO beaconDAO;

	@Autowired
	TrackerDAO trackerDAO;

	@Autowired
	RoomDAO roomDAO;

	@Autowired
	TrackedBeaconDAO trackedBeaconDAO;

	private class TrackerDistance
	{
		private Long roomId;
		private double distance;
		private double posX;
		private double posY;
	}

	@Scheduled( cron = "*/2 * * * * *" ) // every second
	public void run()
	{
		LOGGER.info("refresh position");

		Map<Long, BeaconDTO> beaconDTOMap = beaconDAO.getBeaconIDMap();
		Map<Long, TrackerDTO> trackerDTOMap = trackerDAO.getTrackerIDMap();
		Map<Long, RoomDTO> roomDTOMap = roomDAO.getRoomIDMap();

		// TODO limit to last 10 seconds?
		List<TrackedBeaconDTO> trackedBeaconDTOS = trackedBeaconDAO.getTrackedBeacons();

		Map<BeaconDTO, List<TrackerDistance>> trackedDistances = new HashMap<>();

		// First step: group tracked distances for every beacon
		for( TrackedBeaconDTO trackedBeaconDTO : trackedBeaconDTOS )
		{
			BeaconDTO beaconDTO = beaconDTOMap.get(trackedBeaconDTO.getBeaconId());
			TrackerDTO trackerDTO = trackerDTOMap.get(trackedBeaconDTO.getTrackerId());

			List<TrackerDistance> _trackedDistances = trackedDistances.get(beaconDTO);

			if( _trackedDistances == null )
			{
				_trackedDistances = new ArrayList<>();
				trackedDistances.put(beaconDTO, _trackedDistances);
			}

			TrackerDistance trackerDistance = new TrackerDistance();
			trackerDistance.roomId = trackerDTO.getRoomId();
			trackerDistance.posX = trackerDTO.getPosX();
			trackerDistance.posY = trackerDTO.getPosY();
			// TODO convert to distance
			trackerDistance.distance = LocationHelper.getDistance(trackedBeaconDTO.getRssi(), trackedBeaconDTO.getTxPower() );

			_trackedDistances.add(trackerDistance);
		}

		Map<Long, TrackerDTO> trackerIdMap = trackerDAO.getTrackerIDMap();

		// Second step: Set current Room
		for( BeaconDTO beaconDTO : beaconDTOMap.values() )
		{
			List<TrackerDistance> _trackerDistances = trackedDistances.get(beaconDTO);
			if( _trackerDistances == null || !_trackerDistances.isEmpty() )
			{
				beaconDTO.setPosX(-1);
				beaconDTO.setPosY(-1);
				beaconDTO.setRoomId(null);
			}
			else
			{
				Collections.sort(_trackerDistances, (o1, o2) ->
				{
					if( o1.distance > o2.distance ) return 1;
					if( o1.distance < o2.distance ) return -1;
					return 0;
				});

				double[][] positions = new double[ _trackerDistances.size() ][ 2 ];
				double[] distances = new double[ _trackerDistances.size() ];

				for( int i = 0; i < _trackerDistances.size(); i++ )
				{
					TrackerDistance distance = _trackerDistances.get(i);

					positions[i] = new double[]{ distance.posX, distance.posY };
					distances[i] = distance.distance;
				}

				NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
				LeastSquaresOptimizer.Optimum optimum = solver.solve();

				// the answer
				double[] centroid = optimum.getPoint().toArray();

				RoomDTO roomDTO = roomDTOMap.get(_trackerDistances.get(0).roomId );
				beaconDTO.setRoomId( roomDTO.getId() );
				beaconDTO.setPosX( (int) centroid[0] );
				beaconDTO.setPosY( (int) centroid[1] );

				// error and geometry information; may throw SingularMatrixException depending the threshold argument provided
				//RealVector standardDeviation = optimum.getSigma(0);
				//RealMatrix covarianceMatrix = optimum.getCovariances(0);
			}
		}
	}
}
