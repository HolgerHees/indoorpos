package com.holgerhees.indoorpos.frontend.websockets;

import javax.websocket.Session;

/**
 * Created by hhees on 04.05.17.
 */
public interface EndPointWatcherClient
{
    void notifyNewSession( Session userSession );
}
