package com.holgerhees.web.view;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import com.holgerhees.web.model.Request;

public class JspView extends View
{
	private String jspFilename;

	public JspView(String jspFilename, Request request)
	{
		super(request);
		this.jspFilename = jspFilename;
	}

	@Override
	public void render() throws ServletException, IOException
	{
		RequestDispatcher dispatcher = getRequest().getServletContext().getRequestDispatcher(jspFilename);
		dispatcher.forward(getRequest().getHttpRequest(), getRequest().getHttpResponse());
	}

	public String getJspFilename()
	{
		return jspFilename;
	}
}