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
@Component( "cacheWatcherService" )
public class CacheWatcherService
{
    private static Log LOGGER = LogFactory.getLog( CacheWatcherService.class );
    private static DecimalFormat df = new DecimalFormat( "#.####" );

    // at least every XX ms we should do a websocket call to resynchronize the next wakeup
    public static int PING_INTERVAL = 60000;

    // ble scan interval in ms
    public static int INTERVAL_LENGTH = 2000;

    // beacon advertising interval in ms
    public static int FREQUENCY = 100;

    // time difference in ms between tracking websocket call and cache updates
    public static int WAKEUP_BUFFER = 200;

    // network latency ins ms used to calculate to correct wakeup time
    public static int NETWORK_LATENCY = 20;

    // more then 10 seconds (INTERVAL_LENGTH * 5) active to acitvate "priorised tracker"
    public static int ACTIVE_COUNT_THRESHOLD = 5;
    // more then 3 samples to acitvate "priorised tracker"
    public static int MIN_SAMPLE_THRESHOLD = 3;

    // a new RSSI value of highter then XX should always force a "normal" isActive check
    public static int FORCE_NORMAL_CHECK_RSSI_THRESHOLD = -70;

    // a tracker who tries 3 times (was allready 2 times there) to go active is forcing a "normal" isActive check
    public static int FORCE_NORMAL_CHECK_ATTEMPT_THRESHOLD = 2;

    // a new RSSI value must be higher then XX to force a "normal" isActive check
    public static int FORCE_PRIORITY_CHECK_THRESHOLD = 10;

    @Autowired
    CacheService cacheService;

    private Thread watcher;

    private long lastUpdate;
    private long nextWakeup;

    private static List<CacheWatcherClient> watcherClients = Collections.synchronizedList( new ArrayList<>() );

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

                        for( CacheWatcherClient client : watcherClients )
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

    public void addWatcher( CacheWatcherClient client )
    {
        watcherClients.add( client );
    }

    public long getNextWakeup()
    {
        return nextWakeup - System.currentTimeMillis() - ( WAKEUP_BUFFER - NETWORK_LATENCY );
    }
}
