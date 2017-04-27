package com.holgerhees.indoorpos;

import org.apache.catalina.servlets.DefaultServlet;

import com.holgerhees.indoorpos.frontend.web.model.Request;
import com.holgerhees.indoorpos.frontend.web.view.View;

public interface Router
{
	public View routeRequest(Request request, boolean isPostRequest, DefaultServlet staticContentServlet);
}
