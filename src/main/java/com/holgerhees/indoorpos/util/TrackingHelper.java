package com.holgerhees.indoorpos.util;

import com.holgerhees.indoorpos.frontend.service.CacheService;

public class TrackingHelper
{
    private TrackingHelper()
    {
    }

    public static int compareTracker( CacheService.TrackedBeacon t1, CacheService.TrackedBeacon t2 )
    {
        /*double length1 = LocationHelper.getDistance(  t1.getRssi(), t1.getTxPower() );
        double cmp1 = (100 - length1) * t1.getSamples();

        double length2 = LocationHelper.getDistance(  t2.getRssi(), t2.getTxPower() );
        double cmp2 = (100 - length2) * t2.getSamples();

        if( cmp1 > cmp2 ) return -1;
        if( cmp1 < cmp2 ) return 1;
        return 0;*/

        int signalStrengh1 = 100 + t1.getRssi();
        int priority1 = signalStrengh1 * t1.getSamples();

        int signalStrengh2 = 100 + t2.getRssi();
        int priority2 = signalStrengh2 * t2.getSamples();

        if( priority1 > priority2 ) return -1;
        if( priority1 < priority2 ) return 1;
        return 0;
    }
}
