package com.holgerhees.indoorpos.cron;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.indoorpos.util.LocationHelper;

@Component
public class RefreshPosition
{
	private static Log LOGGER = LogFactory.getLog(RefreshPosition.class);

	@Autowired
	BeaconDAO beaconDAO;

	@Autowired
	TrackerDAO trackerDAO;

	@Autowired
	TrackedBeaconDAO trackedBeaconDAO;

	private class TrackerDistance
	{
		private Long trackerId;
		private double distance;
	}

	@Scheduled( cron = "*/2 * * * * *" ) // every second
	public void run()
	{
		LOGGER.info("refresh position");

		Map<Long, BeaconDTO> beaconDTOMap = beaconDAO.getBeaconIDMap();

		// TODO limit to last 10 seconds?
		List<TrackedBeaconDTO> trackedBeaconDTOS = trackedBeaconDAO.getTrackedBeacons();

		Map<BeaconDTO, List<TrackerDistance>> trackedDistances = new HashMap<>();

		// First step: group tracked distances for every beacon
		for( TrackedBeaconDTO trackedBeaconDTO : trackedBeaconDTOS )
		{
			BeaconDTO beaconDTO = beaconDTOMap.get(trackedBeaconDTO.getBeaconId());

			List<TrackerDistance> _trackedDistances = trackedDistances.get(beaconDTO);

			if( _trackedDistances == null )
			{
				_trackedDistances = new ArrayList<>();
				trackedDistances.put(beaconDTO, _trackedDistances);
			}

			TrackerDistance trackerDistance = new TrackerDistance();
			trackerDistance.trackerId = trackedBeaconDTO.getTrackerId();
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
				beaconDTO.setRoom(null);
			}
			else
			{
				Collections.sort(_trackerDistances, new Comparator<TrackerDistance>()
				{
					@Override
					public int compare(TrackerDistance o1, TrackerDistance o2)
					{
						if( o1.distance > o2.distance )
						{ return 1; }
						if( o1.distance < o2.distance )
						{ return -1; }
						return 0;
					}
				});

				TrackerDTO trackerDTO = trackerIdMap.get(_trackerDistances.get(0).trackerId);
				beaconDTO.setRoom(trackerDTO.getRoom());
			}
		}
	}
}
