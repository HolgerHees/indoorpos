package com.holgerhees.indoorpos.frontend.controller.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.holgerhees.indoorpos.application.ApplicationConfig;
import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.indoorpos.frontend.web.PageDtoInitService;
import com.holgerhees.indoorpos.frontend.web.model.Request;
import com.holgerhees.indoorpos.frontend.web.view.JspView;
import com.holgerhees.indoorpos.frontend.web.view.View;

@Component("homeController")
public class HomeController implements Controller
{
	@Autowired
	PageDtoInitService pageDtoInitService;

	@Override
	public View handle(Request request) {
		
		HomePageDTO ctx = pageDtoInitService.getPageDto(new HomePageDTO(),request);

		return new JspView("/WEB-INF/jsp/home.jsp", request);
	}

}
