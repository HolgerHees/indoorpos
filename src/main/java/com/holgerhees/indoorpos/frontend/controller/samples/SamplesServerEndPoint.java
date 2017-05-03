package com.holgerhees.indoorpos.frontend.controller.samples;

/**
 * Created by hhees on 03.05.17.
 */

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@ServerEndpoint(value="/samplesUpdateTest")
public class SamplesServerEndPoint
{
	private static Log LOGGER = LogFactory.getLog( SamplesServerEndPoint.class);

	private Set<Session> userSessions = Collections.synchronizedSet(new HashSet<>());

	@OnOpen
	public void onOpen(Session userSession) {
		userSessions.add(userSession);
		LOGGER.info("onOpen");
	}

	@OnClose
	public void onClose(Session userSession) {
		userSessions.remove(userSession);
		LOGGER.info("onClose");
	}

	@OnMessage
	public void onMessage(String message, Session userSession) {
		System.out.println("Message Received: " + message);
		for (Session session : userSessions) {
			System.out.println("Sending to " + session.getId());
			session.getAsyncRemote().sendText(message);
		}
	}
}
