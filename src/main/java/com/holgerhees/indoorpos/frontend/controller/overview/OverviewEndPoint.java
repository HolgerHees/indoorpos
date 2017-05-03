package com.holgerhees.indoorpos.frontend.controller.overview;

/**
 * Created by hhees on 03.05.17.
 */

import com.google.gson.JsonElement;
import com.holgerhees.shared.web.util.GSonFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint( value = "/overviewUpdate" )
public class OverviewEndPoint
{
    private static Log LOGGER = LogFactory.getLog( OverviewEndPoint.class );

    private static Set<Session> userSessions = Collections.synchronizedSet( new HashSet<>() );

    private static class Result
    {
        String type;
        Object data;

        public Result( String type, Object data )
        {
            this.type = type;
            this.data = data;
        }
    }

    @OnOpen
    public void onOpen( Session userSession )
    {
        LOGGER.info( "onOpen" );
        userSessions.add( userSession );
        broadcast( "tracker", OverviewWatcher.getCachedTracker() );
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

    public static boolean hasSessions()
    {
        return userSessions.size() > 0;
    }

    public static void broadcast( String type, Object obj )
    {
        Result result = new Result( type, obj );
        JsonElement json = GSonFactory.createGSon().toJsonTree( result );

        for( Session session : userSessions )
        {
            session.getAsyncRemote().sendText( json.toString() );
        }
    }
}
