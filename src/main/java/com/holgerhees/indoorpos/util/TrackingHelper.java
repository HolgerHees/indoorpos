package com.holgerhees.indoorpos.util;

import com.holgerhees.indoorpos.persistance.dao.TrackedBeaconDAO;
import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;

public class TrackingHelper
{
    private static int SAMPLES_THRESHOLD = 20;

    private TrackingHelper()
    {
    }

    public static int compareTracker( TrackedBeaconDTO t1, TrackedBeaconDTO t2 )
    {
        /*double length1 = LocationHelper.getDistance(  t1.getRssi(), t1.getTxPower() );
        double cmp1 = (100 - length1) * t1.getSamples();

        double length2 = LocationHelper.getDistance(  t2.getRssi(), t2.getTxPower() );
        double cmp2 = (100 - length2) * t2.getSamples();

        if( cmp1 > cmp2 ) return -1;
        if( cmp1 < cmp2 ) return 1;
        return 0;*/

        int signalStrengh1 = 100 + t1.getRssi();
        int priority1 = signalStrengh1 * ( t1.getSamples() > SAMPLES_THRESHOLD ? SAMPLES_THRESHOLD : t1.getSamples() ) ;

        int signalStrengh2 = 100 + t2.getRssi();
        int priority2 = signalStrengh2 * ( t2.getSamples() > SAMPLES_THRESHOLD ? SAMPLES_THRESHOLD : t2.getSamples() );

        if( priority1 > priority2 ) return -1;
        if( priority1 < priority2 ) return 1;
        return 0;
    }
}
