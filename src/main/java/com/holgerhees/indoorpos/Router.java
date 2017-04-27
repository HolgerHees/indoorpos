package com.holgerhees.indoorpos;

import org.apache.catalina.servlets.DefaultServlet;

import com.holgerhees.web.model.Request;
import com.holgerhees.web.view.View;

public interface Router
{
	public View routeRequest(Request request, boolean isPostRequest, DefaultServlet staticContentServlet);
}
