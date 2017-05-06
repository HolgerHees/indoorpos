package com.holgerhees.indoorpos.frontend.websockets.samples;

/**
 * Created by hhees on 03.05.17.
 */

import com.google.gson.JsonElement;
import com.holgerhees.indoorpos.frontend.websockets.EndPointWatcherClient;
import com.holgerhees.shared.web.util.GSonFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint( value = "/samplesUpdate" )
@Component( "samplesEndPoint" )
public class SamplesEndPoint
{
    private static Log LOGGER = LogFactory.getLog( SamplesEndPoint.class );

    private static Set<Session> userSessions = Collections.synchronizedSet( new HashSet<>() );
    private static EndPointWatcherClient watcher;

    @OnOpen
    public void onOpen( Session userSession )
    {
        LOGGER.info( "onOpen" );
        userSessions.add( userSession );
        watcher.notifyNewSession( userSession );
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
        LOGGER.info( "onMessage" );
    }

    @OnError
    public void onError( Session session, Throwable t )
    {
        //LOGGER.info( "onError" );
    }

    public static void setSamplesWatcher( EndPointWatcherClient watcher )
    {
        SamplesEndPoint.watcher = watcher;
    }

    public static boolean hasSessions()
    {
        return userSessions.size() > 0;
    }

    public static void broadcastMessage( Object obj )
    {
        JsonElement json = GSonFactory.createGSon().toJsonTree( obj );

        for( Session session : userSessions )
        {
            session.getAsyncRemote().sendText( json.toString() );
        }
    }
}
