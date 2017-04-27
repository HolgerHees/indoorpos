package com.holgerhees.indoorpos.frontend.web.model;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

public class Request {

	private ServletContext servletContext;
	private HttpServletRequest httpRequest;
	private HttpServletResponse httpResponse;
	private String url;
	private String servletPath;
	private Protocol protocol;
	private Map<String, FileItem> parts;
	
	protected Request()
	{
	}

	public Request(HttpServletRequest httpRequest, HttpServletResponse httpResponse, ServletContext servletContext)
	{
		super();
		this.httpRequest = httpRequest;
		this.httpResponse = httpResponse;
		this.servletContext = servletContext;
	}
	
	public ServletContext getServletContext()
	{
		return servletContext;
	}
	public HttpServletRequest getHttpRequest()
	{
		return httpRequest;
	}
	public HttpServletResponse getHttpResponse()
	{
		return httpResponse;
	}

	public String getUrl()
	{
		return url;
	}
	public void setUrl(String url)
	{
		this.url = url;
	}
	public String getServletPath()
	{
		return servletPath;
	}
	public void setServletPath(String servletPath)
	{
		this.servletPath = servletPath;
	}

	public void setValue(String key, Object object)
	{
		httpRequest.setAttribute(key, object);
	}
	
	public Object getValue(String key)
	{
		return httpRequest.getAttribute(key);
	}
	
	public void setPageDTO(PageDTO ctx)
	{
		setValue("ctx", ctx);
	}
	
	public boolean hasPageDTO()
	{
		return httpRequest.getAttribute("ctx") != null;
	}

	public Protocol getProtocol()
	{
		return protocol;
	}
	public void setProtocol(Protocol protocol)
	{
		this.protocol = protocol;
	}

	public void setParts( Map<String, FileItem> parts )
	{
		this.parts = parts;
	}
	
	public FileItem getPart( String key )
	{
		return parts != null ? parts.get(key) : null;
	}
}
