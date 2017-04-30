package com.holgerhees.indoorpos.cron;

import com.holgerhees.indoorpos.persistance.dao.*;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.indoorpos.util.LocationHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

        LOGGER.info( "Refresh positions in " + df
                .format( ( ( System.currentTimeMillis() - start ) / 1000.0f ) ) + " seconds" );
    }
}
