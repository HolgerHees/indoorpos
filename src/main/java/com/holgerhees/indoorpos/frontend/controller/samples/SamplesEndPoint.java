package com.holgerhees.indoorpos.frontend.controller.samples;

/**
 * Created by hhees on 03.05.17.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
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
        LOGGER.info( "onMessage" );
    }

    public static boolean hasSessions()
    {
        return userSessions.size() > 0;
    }

    public static void broadcast( String msg )
    {
        for( Session session : userSessions )
        {
            session.getAsyncRemote().sendText( msg );
        }
    }
}
