package com.holgerhees.indoorpos.frontend.controller.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.indoorpos.frontend.web.model.Request;
import com.holgerhees.indoorpos.frontend.web.view.View;

@Component("mobileApiController")
public class ApiController implements Controller
{
	private static Log LOGGER = LogFactory.getLog(ApiController.class);
	
	@Autowired
	private ApplicationContext applicationContext;

	@Override
	final public View handle(Request req)
	{
		return null;
	}
}
