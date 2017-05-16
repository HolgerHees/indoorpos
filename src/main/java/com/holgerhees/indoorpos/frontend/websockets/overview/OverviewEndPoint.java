package com.holgerhees.indoorpos.frontend.websockets.overview;

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
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ServerEndpoint( value = "/overviewUpdate" )
public class OverviewEndPoint
{
    private static Log LOGGER = LogFactory.getLog( OverviewEndPoint.class );

    private static Set<Session> userSessions = Collections.synchronizedSet( new HashSet<>() );
    private static EndPointWatcherClient watcher;

    @OnOpen
    public void onOpen( Session userSession )
    {
        LOGGER.info( "onOpen" );
        watcher.notifyNewSession( userSession );
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
        LOGGER.info( "onMessage" );
    }

    @OnError
    public void onError( Session session, Throwable t )
    {
        //LOGGER.info( "onError" );
    }

    public static void setWatcher( EndPointWatcherClient watcher )
    {
        OverviewEndPoint.watcher = watcher;
    }

    public static boolean hasSessions()
    {
        return userSessions.size() > 0;
    }

    public static void sendMessage( Session session, String message )
    {
        try
        {
            // must be synchron
            session.getBasicRemote().sendText( message );
        }
        catch( IOException e ){}
    }

    public static void broadcastMessage( String message )
    {
        for( Session session : userSessions )
        {
            session.getAsyncRemote().sendText( message );
        }
    }
}
