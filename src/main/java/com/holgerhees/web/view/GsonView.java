package com.holgerhees.web.view;

import java.io.IOException;

import javax.servlet.ServletException;

import com.holgerhees.web.model.Request;
import com.holgerhees.web.util.GSonFactory;
import com.google.gson.JsonElement;

public class GsonView extends View{

	private String gson;

	public GsonView(JsonElement jsonElement, Request req)
	{
		super(req);
		this.gson = GSonFactory.createGSon().toJson(jsonElement);
	}

	@Override
	public void render() throws ServletException, IOException
	{
		getRequest().getHttpResponse().setContentType("application/json");
		getRequest().getHttpResponse().setCharacterEncoding("utf-8");

		getRequest().getHttpResponse().getWriter().print(gson);
	}

	public String toJSON(){
		return this.gson;
	}
}
