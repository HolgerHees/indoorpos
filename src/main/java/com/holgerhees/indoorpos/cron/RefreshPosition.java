package com.holgerhees.indoorpos.cron;

import com.holgerhees.indoorpos.persistance.dao.*;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.DetectedRoomsDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.indoorpos.util.LocationHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.*;

@Component
public class RefreshPosition
{
    private static Log LOGGER = LogFactory.getLog( RefreshPosition.class );
    private static DecimalFormat df = new DecimalFormat( "#.###" );

    private static int REFERENCE_WIDTH_PX = 1000;
    private static double REFERENCE_WIDTH_METER = 14.62;
    private static int REFERENCE_HEIGHT_PX = 736;
    private static double REFERENCE_HEIGHT_METER = 10.77;

    private static int PADDING = 50;

    @Autowired
    BeaconDAO beaconDAO;

    @Autowired
    TrackerDAO trackerDAO;

    @Autowired
    RoomDAO roomDAO;

    @Autowired
    DetectedRoomsDAO detectedRoomsDAO;

    @Autowired
    TrackedBeaconDAO trackedBeaconDAO;

    private class TrackerDistance
    {
        private Long roomId;
        private double distance;
        private double posX;
        private double posY;
    }

    //@Scheduled( cron = "*/5 * * * * *" ) // every second
    public void run()
    {
        final long start = System.currentTimeMillis();

        Map<Long, TrackerDTO> trackerDTOMap = trackerDAO.getTrackerIDMap();

        // TODO limit to last 10 seconds?
        List<TrackedBeaconDTO> trackedBeaconDTOS = trackedBeaconDAO.getActiveTrackedBeacons();

        Map<Long, List<TrackerDistance>> trackedDistances = new HashMap<>();

        // First step: group tracked distances for every beacon
        for( TrackedBeaconDTO trackedBeaconDTO : trackedBeaconDTOS )
        {
            TrackerDTO trackerDTO = trackerDTOMap.get( trackedBeaconDTO.getTrackerId() );

            List<TrackerDistance> _trackedDistances = trackedDistances.get( trackedBeaconDTO.getBeaconId() );

            if( _trackedDistances == null )
            {
                _trackedDistances = new ArrayList<>();
                trackedDistances.put( trackedBeaconDTO.getBeaconId(), _trackedDistances );
            }

            TrackerDistance trackerDistance = new TrackerDistance();
            trackerDistance.roomId = trackerDTO.getRoomId();
            trackerDistance.posX = trackerDTO.getPosX();
            trackerDistance.posY = trackerDTO.getPosY();
            // TODO convert to distance
            double distanceInMeter = LocationHelper.getDistance( trackedBeaconDTO.getRssi(), trackedBeaconDTO.getTxPower() );

            trackerDistance.distance = distanceInMeter * REFERENCE_WIDTH_PX / REFERENCE_WIDTH_METER;

            //LOGGER.info( trackerDTO.getName() + " " + trackerDistance.distance + " (" + distanceInMeter + ") " + trackedBeaconDTO.getRssi() + " " + trackedBeaconDTO.getTxPower() );

            _trackedDistances.add( trackerDistance );
        }

        Map<Long,List<DetectedRoomsDTO>> detectedRoomsDTOs = detectedRoomsDAO.getDetectedRoomsByBeacon();

        List<BeaconDTO> beaconDTOs = beaconDAO.getBeacons();

        // Second step: Set current Room
        for( BeaconDTO beaconDTO : beaconDTOs )
        {
            List<DetectedRoomsDTO> rooms = detectedRoomsDTOs.get(  beaconDTO.getId() );

            List<TrackerDistance> _trackerDistances = trackedDistances.get( beaconDTO.getId() );
            if( _trackerDistances != null && !_trackerDistances.isEmpty() )
            {
                Collections.sort( _trackerDistances, ( o1, o2) ->
                {
					if( o1.distance > o2.distance )
					{ return 1; }
					if( o1.distance < o2.distance )
					{ return -1; }
					return 0;
				});

                for( TrackerDistance trackerDistance: _trackerDistances )
                {
                    boolean found = false;
                    if( rooms != null )
                    {
                        for( DetectedRoomsDTO room : rooms )
                        {
                            if( !trackerDistance.roomId.equals( room.getRoomId() ) )
                            {
                                continue;
                            }

                            rooms.remove( room );
                            found = true;

                            int distance = (int) ( trackerDistance.distance * 100 );

                            if( room.getDistance() != distance )
                            {
                                room.setDistance( distance );
                                detectedRoomsDAO.save( room );
                            }
                            break;
                        }
                    }
                    if( !found )
                    {
                        DetectedRoomsDTO room = new DetectedRoomsDTO();
                        room.setBeaconId(  beaconDTO.getId() );
                        room.setRoomId( trackerDistance.roomId );
                        room.setDistance( (int) ( trackerDistance.distance * 100 ) );
                        detectedRoomsDAO.save( room );
                    }
                }

                /*double[][] positions = new double[_trackerDistances.size()][2];
                double[] distances = new double[_trackerDistances.size()];

                for( int i = 0; i < _trackerDistances.size(); i++ )
                {
                    TrackerDistance distance = _trackerDistances.get( i );

                    positions[i] = new double[]{ distance.posX, distance.posY };
                    distances[i] = distance.distance;
                }

                double[] pos;
                if( positions.length == 1 )
                {
                    pos = positions[0];
                    // TODO distances[0] not used
                }
                else
                {
                    NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver( new TrilaterationFunction( positions, distances ),
                                                                                          new LevenbergMarquardtOptimizer() );
                    LeastSquaresOptimizer.Optimum optimum = solver.solve();

                    // the answer
                    pos = optimum.getPoint().toArray();
                }

                if( _trackerDistances.get( 0 ).roomId.equals( beaconDTO.getRoomId() ) && beaconDTO.getPosX() == (int) pos[0] && beaconDTO.getPosY() == (int) pos[1] )
                {
                    continue;
                }

                if( pos[0] < PADDING ) pos[0] = PADDING;
                else if( pos[0] > REFERENCE_WIDTH_PX - PADDING ) pos[0] = REFERENCE_WIDTH_PX - PADDING;

                if( pos[1] < PADDING ) pos[1] = PADDING;
                else if( pos[1] < REFERENCE_HEIGHT_METER - PADDING ) pos[1] = REFERENCE_HEIGHT_METER - PADDING;

                beaconDTO.setRoomId( _trackerDistances.get( 0 ).roomId );
                beaconDTO.setPosX( (int) pos[0] );
                beaconDTO.setPosY( (int) pos[1] );*/

                // error and geometry information; may throw SingularMatrixException depending the threshold argument provided
                //RealVector standardDeviation = optimum.getSigma(0);
                //RealMatrix covarianceMatrix = optimum.getCovariances(0);
            }
        }

        for( Long beaconId: detectedRoomsDTOs.keySet() )
        {
            for( DetectedRoomsDTO room: detectedRoomsDTOs.get( beaconId ) )
            {
                detectedRoomsDAO.delete( room.getId() );
            }
        }

        LOGGER.info( "Refresh positions in " + df
                .format( ( ( System.currentTimeMillis() - start ) / 1000.0f ) ) + " seconds" );
    }
}
