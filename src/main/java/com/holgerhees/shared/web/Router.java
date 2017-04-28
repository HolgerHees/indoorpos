package com.holgerhees.shared.web;

import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.view.View;
import org.apache.catalina.servlets.DefaultServlet;

import javax.servlet.ServletException;

public interface Router
{
    View routeRequest( Request request, boolean isPostRequest, DefaultServlet staticContentServlet ) throws ServletException;
}
