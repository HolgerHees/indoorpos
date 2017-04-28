package com.holgerhees.indoorpos.util;

public class LocationHelper
{
    private LocationHelper()
    {
    }

    public static double getDistance( int rssi, int txPower )
    {
    /*
     * RSSI = TxPower - 10 * n * lg(d)
     * n = 2 (in free space)
     *
     * d = 10 ^ ((TxPower - RSSI) / (10 * n))
     */

        return Math.pow( 10d, ( (double) txPower - rssi ) / ( 10 * 2 ) );
    }

    public static double getDistance( double _lat1, double _lon1, double _lat2, double _lon2 )
    {
        int R = 6371000; // m
        double dLat = Math.toRadians( _lat2 - _lat1 );
        double dLon = Math.toRadians( _lon2 - _lon1 );
        double lat1 = Math.toRadians( _lat1 );
        double lat2 = Math.toRadians( _lat2 );

        double a = Math.sin( dLat / 2 ) * Math.sin( dLat / 2 ) + Math.sin( dLon / 2 ) * Math.sin( dLon / 2 ) * Math.cos( lat1 ) * Math.cos( lat2 );
        double c = 2 * Math.atan2( Math.sqrt( a ), Math.sqrt( 1 - a ) );
        double d = R * c;

        return d;
    }

	/*public static double getDistance(double lat1, double lon1, double lat2, double lon2) 
    {
		if( lat1 == lat2 && lon1 == lon2 ) return 0.0;
		
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		dist = dist * 1.609344;
		return (dist);
	}
	
	private static double deg2rad(double deg)
	{
		return (deg * Math.PI / 180.0);
	}
	
	private static double rad2deg(double rad)
	{
		return (rad * 180 / Math.PI);
	}*/
}
