package com.holgerhees.indoorpos;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.servlets.DefaultServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.holgerhees.shared.web.model.Protocol;
import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.util.RequestUtils;
import com.holgerhees.shared.web.view.View;

public class RouterServlet extends HttpServlet
{

	private static final long serialVersionUID = 1L;

	private static Log LOGGER = LogFactory.getLog(RouterServlet.class);

	private ApplicationContext applicationContext;
	private Application application;
	private Router router = null;
	private static DefaultServlet staticContentServlet = new DefaultServlet();

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		staticContentServlet.init(config);

		application = getApplication();
		applicationContext = application.initialize(config.getServletContext());
		router = application.getRouter();

		if( router == null )
		{
			LOGGER.error("Application.getRouter() returned null pointer. Please return a Router instance.");
			System.exit(-1);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doRequestProcessing(request, response, false);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doRequestProcessing(request, response, true);
	}

	private final void doRequestProcessing(HttpServletRequest request, HttpServletResponse response, boolean isPostRequest) throws ServletException
	{
		try
		{
			Request req = createRequest(request, response);

			View view = router.routeRequest(req, isPostRequest, staticContentServlet);

			if( view != null )
			{
				view.render();
			}
		}
		catch( IOException ioException )
		{
			throw new ServletException("this shouldnt happen. Converting Exception to ServletException", ioException);
		}
	}

	private Request createRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
	{
		final String url = httpRequest.getRequestURL().toString();

		Request request = new Request(httpRequest, httpResponse, getServletContext());
		request.setUrl(url);
		request.setServletPath(httpRequest.getServletPath());
		request.setProtocol(Protocol.fromUrlName(RequestUtils.getProtocolFromUrl(url)));

		return request;
	}

	private Application getApplication()
	{
		try
		{
			@SuppressWarnings( "rawtypes" ) Class applicationClass = Class.forName(getServletContext().getInitParameter("applicationClass"));
			LOGGER.info("instantiating application descriptor class [" + applicationClass + "]");
			return (Application) applicationClass.newInstance();
		}
		catch( ClassNotFoundException e )
		{
			throw new IllegalArgumentException(
				"the web.xml appears to be missing the [applicationClass] parameter. Please add necessary <context-param> elements.", e);
		}
		catch( InstantiationException | IllegalAccessException e )
		{
			throw new IllegalArgumentException("could not create instance of class " + applicationContext, e);
		}
	}

	@Override
	public void destroy()
	{
		super.destroy();

		application.shutdown();
	}
}
