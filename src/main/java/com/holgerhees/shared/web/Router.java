package com.holgerhees.shared.web;

import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.view.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public interface Router
{
    View routeRequest( Request request, boolean isPostRequest, HttpServlet staticContentServlet ) throws ServletException;
}
