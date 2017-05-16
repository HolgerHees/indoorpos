package com.holgerhees.indoorpos.frontend.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by hhees on 04.05.17.
 */
@Component( "cacheServiceBuilderJob" )
public class CacheServiceBuilderJob
{
    private static Log LOGGER = LogFactory.getLog( CacheServiceBuilderJob.class);
    private static DecimalFormat df = new DecimalFormat( "#.####" );

    // at least every XX ms we should do a websocket call to resynchronize the next wakeup
    public static int PING_INTERVAL = 60000;

    // ble scan interval in ms
    public static int INTERVAL_LENGTH = 1000;

    // beacon advertising interval in ms
    public static int FREQUENCY = 100;

    // time difference in ms between tracking websocket call and cache updates
    public static int WAKEUP_BUFFER = 200;

    // network latency ins ms used to calculate to correct wakeup time
    public static int NETWORK_LATENCY = 20;

    /** is Active check **/
    // "priorised tracker" only if tracker is active more then XX times
    public static int ACTIVE_COUNT_THRESHOLD = 5;
	// if the last active tracker is not tracked anymore, use it XX times as a fallback
	public static int MAX_FALLBACK_COUNT = 5;
	// if the tracker is not tracked anymore more then XX times, ignore history data
    public static int OUTDATED_COUNT = 5;
    // reference variance
	public static double REFERENCE_VARIANCE = 80.0;

	public static int MAP_WIDTH = 1000;
	public static int MAP_HEIGHT = 736;
    /*********************/

    @Autowired
    CacheService cacheService;

    private Thread watcher;

    private long lastUpdate;
    private long nextWakeup;

    private static List<CacheServiceBuilderClient> watcherClients = Collections.synchronizedList( new ArrayList<>());

    public class Watcher implements Runnable
    {
        public Watcher()
        {
        }

        @Override
        public void run()
        {
            while( !Thread.currentThread().isInterrupted() )
            {
                try
                {
                    nextWakeup = System.currentTimeMillis() + INTERVAL_LENGTH;
                    Thread.sleep( INTERVAL_LENGTH );
                    if( lastUpdate != cacheService.getLastUpdate() )
                    {
                        lastUpdate = cacheService.getLastUpdate();

                        long start = System.currentTimeMillis();

                        cacheService.updateActiveTracker();
                        //cacheServiceNew.updateActiveTracker();

                        for( CacheServiceBuilderClient client : watcherClients )
                        {
                            client.notifyCacheChange();
                        }

                        LOGGER.info( "Update cache " + df.format( ( ( System.currentTimeMillis() - start ) / 1000.0f ) ) + " seconds" );
                    }
                }
                catch( InterruptedException e )
                {
                    //e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                catch( Exception e )
                {
                    LOGGER.fatal( e );
                    e.printStackTrace();
                }
            }
        }
    }

    @PostConstruct
    public void init()
    {
        watcher = new Thread( new Watcher() );
        watcher.setDaemon( true );
        watcher.start();
    }

    public void shutdown()
    {
        LOGGER.info("Shutting down CacheWatcherService" );
        watcher.interrupt();
    }

    public void addWatcher( CacheServiceBuilderClient client)
    {
        watcherClients.add( client );
    }

    public long getNextWakeup()
    {
        return nextWakeup - System.currentTimeMillis() - ( WAKEUP_BUFFER - NETWORK_LATENCY );
    }
}
