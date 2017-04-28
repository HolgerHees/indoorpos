package com.holgerhees.shared.web;

import org.springframework.context.ApplicationContext;

import javax.servlet.ServletContext;

public interface Application
{
    ApplicationContext initialize( ServletContext servletContext );

    void shutdown();

    Router getRouter();
}
