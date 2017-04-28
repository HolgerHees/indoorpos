package com.holgerhees.indoorpos.cron;

import com.holgerhees.indoorpos.persistance.dao.BeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.RoomDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackedBeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.indoorpos.util.LocationHelper;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RefreshPosition
{
    private static Log LOGGER = LogFactory.getLog(RefreshPosition.class);
    private static DecimalFormat df = new DecimalFormat("#.###");

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
        final long start = System.currentTimeMillis();

        Map<Long, TrackerDTO> trackerDTOMap = trackerDAO.getTrackerIDMap();

        // TODO limit to last 10 seconds?
        List<TrackedBeaconDTO> trackedBeaconDTOS = trackedBeaconDAO.getTrackedBeacons();

        Map<Long, List<TrackerDistance>> trackedDistances = new HashMap<>();

        // First step: group tracked distances for every beacon
        for( TrackedBeaconDTO trackedBeaconDTO : trackedBeaconDTOS )
        {
            TrackerDTO trackerDTO = trackerDTOMap.get(trackedBeaconDTO.getTrackerId());

            List<TrackerDistance> _trackedDistances = trackedDistances.get(trackedBeaconDTO.getBeaconId());

            if( _trackedDistances == null )
            {
                _trackedDistances = new ArrayList<>();
                trackedDistances.put(trackedBeaconDTO.getBeaconId(), _trackedDistances);
            }

            TrackerDistance trackerDistance = new TrackerDistance();
            trackerDistance.roomId = trackerDTO.getRoomId();
            trackerDistance.posX = trackerDTO.getPosX();
            trackerDistance.posY = trackerDTO.getPosY();
            // TODO convert to distance
            trackerDistance.distance = LocationHelper.getDistance(trackedBeaconDTO.getRssi(), trackedBeaconDTO.getTxPower());

            _trackedDistances.add(trackerDistance);
        }

        List<BeaconDTO> beaconDTOs = beaconDAO.getBeacons();

        // Second step: Set current Room
        for( BeaconDTO beaconDTO : beaconDTOs )
        {
            List<TrackerDistance> _trackerDistances = trackedDistances.get(beaconDTO.getId());
            if( _trackerDistances == null || !_trackerDistances.isEmpty() )
            {
                if( beaconDTO.getRoomId() == null )
                {
                    continue;
                }
                beaconDTO.setPosX(-1);
                beaconDTO.setPosY(-1);
                beaconDTO.setRoomId(null);
            } else
            {
                /*Collections.sort(_trackerDistances, (o1, o2) ->
                {
					if( o1.distance > o2.distance )
					{ return 1; }
					if( o1.distance < o2.distance )
					{ return -1; }
					return 0;
				});*/

                double[][] positions = new double[_trackerDistances.size()][2];
                double[] distances = new double[_trackerDistances.size()];

                for( int i = 0; i < _trackerDistances.size(); i++ )
                {
                    TrackerDistance distance = _trackerDistances.get(i);

                    positions[i] = new double[]{distance.posX, distance.posY};
                    distances[i] = distance.distance;
                }

                NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances),
                        new LevenbergMarquardtOptimizer());
                LeastSquaresOptimizer.Optimum optimum = solver.solve();

                // the answer
                double[] centroid = optimum.getPoint().toArray();

                beaconDTO.setRoomId(_trackerDistances.get(0).roomId);
                beaconDTO.setPosX((int) centroid[0]);
                beaconDTO.setPosY((int) centroid[1]);

                // error and geometry information; may throw SingularMatrixException depending the threshold argument provided
                //RealVector standardDeviation = optimum.getSigma(0);
                //RealMatrix covarianceMatrix = optimum.getCovariances(0);
            }

            beaconDAO.save(beaconDTO);
        }

        LOGGER.info("Refresh positions in " + df
                .format(((System.currentTimeMillis() - start) / 1000.0f)) + " seconds");
    }
}
