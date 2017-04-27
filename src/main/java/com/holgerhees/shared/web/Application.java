package com.holgerhees.shared.web;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;

public interface Application
{
	public ApplicationContext initialize(ServletContext servletContext);

	public void shutdown();

	public Router getRouter();
}
