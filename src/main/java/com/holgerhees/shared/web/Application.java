package com.holgerhees.shared.web;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;

public interface Application
{
	ApplicationContext initialize(ServletContext servletContext);

	void shutdown();

	Router getRouter();
}
