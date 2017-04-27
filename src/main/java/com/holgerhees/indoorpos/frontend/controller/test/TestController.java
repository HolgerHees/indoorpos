package com.holgerhees.indoorpos.frontend.controller.test;

import org.springframework.stereotype.Component;

import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.indoorpos.frontend.web.model.Request;
import com.holgerhees.indoorpos.frontend.web.view.JspView;
import com.holgerhees.indoorpos.frontend.web.view.View;

@Component("testController")
public class TestController implements Controller
{

	@Override
	public View handle(Request request) {
		
		return new JspView("/WEB-INF/jsp/test.jsp", request);
	}

}
