package com.holgerhees.indoorpos.frontend;

import java.text.DecimalFormat;

import org.apache.catalina.servlets.DefaultServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.holgerhees.indoorpos.ApplicationConfig;
import com.holgerhees.shared.web.Router;
import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.shared.web.service.PageDtoInitService;
import com.holgerhees.shared.web.model.PageDTO;
import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.view.View;

@Component( "frontendRouter" )
public class FrontendRouter implements Router
{
	private static Log LOGGER = LogFactory.getLog(Router.class);
	private static DecimalFormat df = new DecimalFormat("#.###");

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ApplicationConfig config;

	@Autowired
	private PageDtoInitService pageDtoInitService;

	public View routeRequest(Request request, boolean isPostRequest, DefaultServlet staticContentServlet)
	{

		final long start = System.currentTimeMillis();

		Controller controller = null;

		if( request.getServletPath().startsWith("/tracker/") )
		{
			controller = (Controller) applicationContext.getBean("trackerController");
		}
		else if( request.getServletPath().startsWith("/test/") )
		{
			controller = (Controller) applicationContext.getBean("testController");
		}
		else if( request.getServletPath().startsWith("/overview/") )
		{
			controller = (Controller) applicationContext.getBean("overviewController");
		}
		else if( request.getServletPath().startsWith("/overviewTracker/") )
		{
			controller = (Controller) applicationContext.getBean("overviewTrackerController");
		}
		else if( request.getServletPath().startsWith("/overviewBeacon/") )
		{
			controller = (Controller) applicationContext.getBean("overviewBeaconController");
		}
		else if( isStaticContent(request) )
		{
			controller = getStaticContentController(request, staticContentServlet);
		}
		else
		{
			controller = (Controller) applicationContext.getBean("homeController");
		}

		View view = null;
		if( controller != null )
		{
			view = controller.handle(request);

			LOGGER.info("Handle '" + request.getServletPath() + " with '" + controller.getClass().getName() + "' in " + df
				.format(((System.currentTimeMillis() - start) / 1000.0f)) + " seconds");

			if( !request.hasPageDTO() )
			{
				pageDtoInitService.getPageDto(new PageDTO(), request);
			}
		}

		return view;
	}

	private Controller getStaticContentController(Request request, DefaultServlet staticContentServlet)
	{
		request.getHttpResponse().setDateHeader("Expires", System.currentTimeMillis() + 2764800000L);
		request.getHttpResponse().setHeader("Vary", "Accept-Encoding");
		request.getHttpResponse().setHeader("Cache-Control", "public");
		request.setValue("staticContentServlet", staticContentServlet);
		return (Controller) applicationContext.getBean("fileController");
	}

	private boolean isStaticContent(Request request)
	{
		final String path = request.getServletPath();
		return path.startsWith("/css/") || path.startsWith("/img/") || path.startsWith("/js/") || path.endsWith("favicon.ico") || path
			.endsWith("robots.txt");
	}
}
