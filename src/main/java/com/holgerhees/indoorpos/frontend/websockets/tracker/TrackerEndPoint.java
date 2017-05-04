package com.holgerhees.indoorpos.frontend.websockets.tracker;

/**
 * Created by hhees on 03.05.17.
 */

import com.google.gson.JsonElement;
import com.holgerhees.indoorpos.frontend.websockets.EndPointWatcherClient;
import com.holgerhees.shared.web.util.GSonFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint( value = "/trackerUpdate" )
public class TrackerEndPoint
{
    private static Log LOGGER = LogFactory.getLog( TrackerEndPoint.class );
    private static DecimalFormat df = new DecimalFormat( "#.###" );

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
        long start = System.currentTimeMillis();
        watcher.notifyTrackerChange( message );
        LOGGER.info( "Handle tracker message in " + df
                .format( ( ( System.currentTimeMillis() - start ) / 1000.0f ) ) + " seconds"  );
    }

	@OnError
	public void onError( Session session, Throwable t )
	{
		//LOGGER.info( "onError" );
	}

	public static void setTrackerWatcher( TrackerWatcher watcher)
	{
		TrackerEndPoint.watcher = watcher;
	}
}
