package com.holgerhees.indoorpos;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;

import com.holgerhees.indoorpos.Router;

public interface Application {

	public ApplicationContext initialize(ServletContext servletContext);

	public void shutdown();

	public Router getRouter();
}
