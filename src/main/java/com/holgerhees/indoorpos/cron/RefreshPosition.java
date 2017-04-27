package com.holgerhees.indoorpos.cron;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.holgerhees.indoorpos.persistance.dao.BeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackedBeaconDAO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;

@Component
public class RefreshPosition
{
	private static Log LOGGER = LogFactory.getLog(RefreshPosition.class);

	@Autowired
	BeaconDAO beaconDAO;

	@Autowired
	TrackedBeaconDAO trackedBeaconDAO;

	private class TrackerDistance
	{
		private Long trackerId;
		private int distance;
	}

	@Scheduled(cron="*/2 * * * * *") // every second
    public void run()
	{
		LOGGER.info("refresh position");

		Map<Long,BeaconDTO> beaconDTOMap = beaconDAO.getBeaconIDMap();

		List<TrackedBeaconDTO> trackedBeaconDTOS = trackedBeaconDAO.getTrackedBeacons();

		Map<BeaconDTO,List<TrackerDistance>> trackedDistances = new HashMap<>();

		// First step: group tracked distances for every beacon
		for( TrackedBeaconDTO trackedBeaconDTO: trackedBeaconDTOS )
		{
			BeaconDTO beaconDTO = beaconDTOMap.get( trackedBeaconDTO.getBeaconId() );

			List<TrackerDistance> _trackedDistances = trackedDistances.get( beaconDTO );

			if( _trackedDistances == null )
			{
				_trackedDistances = new ArrayList<>();
				trackedDistances.put( beaconDTO, _trackedDistances );
			}

			TrackerDistance trackerDistance = new TrackerDistance();
			trackerDistance.trackerId = trackedBeaconDTO.getTrackerId();
			// TODO convert to distance
			trackerDistance.distance = trackedBeaconDTO.getPower();

			_trackedDistances.add(trackerDistance);
		}

		// Second step:
		for(  Map.Entry<BeaconDTO,List<TrackerDistance>> entry: trackedDistances.entrySet() )
		{
			BeaconDTO beaconDTO = entry.getKey();
			List<TrackerDistance> trackerDistances = entry.getValue();

			LOGGER.info( "Beacon: " + beaconDTO.getUuid() );
			for( TrackerDistance trackerDistance: trackerDistances )
			{
				LOGGER.info( "\tTracker: " + trackerDistance.trackerId + " " + trackerDistance.distance );
			}
		}
    }
}
