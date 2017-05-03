package com.holgerhees.indoorpos.frontend;

import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.shared.web.Router;
import com.holgerhees.shared.web.model.PageDTO;
import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.service.PageDtoInitService;
import com.holgerhees.shared.web.view.View;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import java.text.DecimalFormat;

@Component( "frontendRouter" )
public class FrontendRouter implements Router
{
    private static Log LOGGER = LogFactory.getLog( Router.class );
    private static DecimalFormat df = new DecimalFormat( "#.###" );

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PageDtoInitService pageDtoInitService;

    @Override
    public View routeRequest( Request request, boolean isPostRequest, DefaultServlet staticContentServlet ) throws ServletException
    {
        final long start = System.currentTimeMillis();

        Controller controller;

        if( isStaticContent( request ) )
        {
            controller = getStaticContentController( request, staticContentServlet );
        }
        else
        {
        	try
	        {
	        	int length = request.getServletPath().length();
		        String name = length <= 2 ? "homeController" : request.getServletPath().substring(1, length - 1) + "Controller";
		        controller = (Controller) applicationContext.getBean(name);
	        }
	        catch( BeansException e )
	        {
		        controller = (Controller) applicationContext.getBean( "homeController" );
	        }
        }

        View view = null;
        if( controller != null )
        {
            try
            {
                view = controller.handle( request );

                //LOGGER.info( "Handle '" + request.getServletPath() + " with '" + controller.getClass().getSimpleName() + "' in " + df
	            //        .format( ( ( System.currentTimeMillis() - start ) / 1000.0f ) ) + " seconds" );

                if( !request.hasPageDTO() )
                {
                    pageDtoInitService.getPageDto( new PageDTO(), request );
                }
            } catch( Exception e )
            {
                throw new ServletException( e );
            }
        }
        else
        {
            LOGGER.info( "Handle '" + request.getServletPath() + " not found" );
        }

        return view;
    }

    private Controller getStaticContentController( Request request, DefaultServlet staticContentServlet )
    {
        request.getHttpResponse().setDateHeader( "Expires", System.currentTimeMillis() + 2764800000L );
        request.getHttpResponse().setHeader( "Vary", "Accept-Encoding" );
        request.getHttpResponse().setHeader( "Cache-Control", "public" );
        request.setValue( "staticContentServlet", staticContentServlet );
        return (Controller) applicationContext.getBean( "fileController" );
    }

    private boolean isStaticContent( Request request )
    {
        final String path = request.getServletPath();
        return path.startsWith( "/css/" ) || path.startsWith( "/img/" ) || path.startsWith( "/js/" ) || path.endsWith( "favicon.ico" ) || path
                .endsWith( "robots.txt" );
    }
}
