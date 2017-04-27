package com.holgerhees.shared.web.view;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.servlets.DefaultServlet;

import com.holgerhees.shared.web.model.Request;

public class FileView extends View
{

	private DefaultServlet defaultServlet;

	public FileView(DefaultServlet defaultServlet, Request request)
	{
		super(request);
		this.defaultServlet = defaultServlet;
	}

	@Override
	public void render() throws ServletException, IOException
	{
		defaultServlet.service(getRequest().getHttpRequest(), getRequest().getHttpResponse());
	}
}
