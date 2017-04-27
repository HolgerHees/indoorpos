package com.holgerhees.web.view;

import java.io.IOException;

import javax.servlet.ServletException;

import com.holgerhees.web.model.Request;

public abstract class View
{

	private Request request;

	public View(Request request)
	{
		this.request = request;
	}

	public abstract void render() throws ServletException, IOException;

	public Request getRequest()
	{
		return request;
	}
}
