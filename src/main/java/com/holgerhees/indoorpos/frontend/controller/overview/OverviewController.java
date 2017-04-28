package com.holgerhees.indoorpos.frontend.controller.overview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.service.PageDtoInitService;
import com.holgerhees.shared.web.view.JspView;
import com.holgerhees.shared.web.view.View;

@Component( "overviewController" )
public class OverviewController implements Controller
{
	@Autowired
	PageDtoInitService pageDtoInitService;

	@Override
	public View handle(Request request)
	{
		OverviewPageDTO ctx = pageDtoInitService.getPageDto(new OverviewPageDTO(), request);

		return new JspView("/WEB-INF/jsp/overview.jsp", request);
	}

}
