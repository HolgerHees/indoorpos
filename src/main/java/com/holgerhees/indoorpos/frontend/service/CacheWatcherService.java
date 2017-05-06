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

    // if more then 10 seconds active (INTERVAL_LENGTH * 5)
    public static int ACTIVE_COUNT_THRESHOLD = 5;
    // more then 50% of possible samples (INTERVAL_LENGTH / FREQUENCY)
    public static int MIN_SAMPLE_THRESHOLD = 10;

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
                } catch( InterruptedException e )
                {
                    //e.printStackTrace();
                    Thread.currentThread().interrupt();
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
        watcher.interrupt();
    }

    public void addWatcher( CacheWatcherClient client )
    {
        watcherClients.add( client );
    }

    public long getNextWakeup()
    {
        return nextWakeup - System.currentTimeMillis() - WAKEUP_BUFFER;
    }
}
