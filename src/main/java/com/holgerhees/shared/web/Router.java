package com.holgerhees.shared.web;

import org.apache.catalina.servlets.DefaultServlet;

import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.view.View;

public interface Router
{
	public View routeRequest(Request request, boolean isPostRequest, DefaultServlet staticContentServlet);
}
