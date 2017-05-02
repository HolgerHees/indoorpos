package com.holgerhees.indoorpos.util;

import com.holgerhees.indoorpos.persistance.dao.TrackedBeaconDAO;
import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;

public class TrackingHelper
{
    private TrackingHelper()
    {
    }

    public static int compareTracker( TrackedBeaconDTO t1, TrackedBeaconDTO t2 )
    {
        if( t2.getSamples() > 22 )
        {
            return -1;
        }

        int signalStrengh1 = 100 + t1.getRssi();
        int priority1 = signalStrengh1 * t1.getSamples();

        int signalStrengh2 = 100 + t2.getRssi();
        int priority2 = signalStrengh2 * t2.getSamples();

        if( priority1 > priority2 ) return -1;
        if( priority1 < priority2 ) return 1;
        return 0;
    }
}
