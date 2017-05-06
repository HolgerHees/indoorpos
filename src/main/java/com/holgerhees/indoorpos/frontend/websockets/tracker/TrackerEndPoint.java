package com.holgerhees.indoorpos.frontend.websockets.tracker;

/**
 * Created by hhees on 03.05.17.
 */

import com.holgerhees.indoorpos.frontend.service.CacheWatcherService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint( value = "/trackerUpdate" )
public class TrackerEndPoint
{
    private static Log LOGGER = LogFactory.getLog( TrackerEndPoint.class );

    private static Set<Session> userSessions = Collections.synchronizedSet( new HashSet<>() );
    private static TrackerWatcher watcher;

    @OnOpen
    public void onOpen( Session userSession )
    {
        LOGGER.info( "onOpen" );
        userSessions.add( userSession );
    }

    @OnClose
    public void onClose( Session userSession )
    {
        LOGGER.info( "onClose" );
        userSessions.remove( userSession );
    }

    @OnMessage
    public void onMessage( String message, Session userSession )
    {
        //LOGGER.info( "onMessage" );

        if( message.equals( "init" ) )
        {
            String msg = Long.toString( watcher.getNextWakeup() ) + "," + Long.toString( CacheWatcherService.INTERVAL_LENGTH ) + "," + Long.toString( CacheWatcherService.FREQUENCY ) + "," + Long.toString( CacheWatcherService.PING_INTERVAL );
            userSession.getAsyncRemote().sendText( msg );
        }
        else
        {
            watcher.notifyTrackerChange( message );
            userSession.getAsyncRemote().sendText( Long.toString( watcher.getNextWakeup() ) );
        }
    }

    @OnError
    public void onError( Session session, Throwable t )
    {
        LOGGER.info( "onError" );
        t.printStackTrace();
    }

    public static void setTrackerWatcher( TrackerWatcher watcher )
    {
        TrackerEndPoint.watcher = watcher;
    }
}
