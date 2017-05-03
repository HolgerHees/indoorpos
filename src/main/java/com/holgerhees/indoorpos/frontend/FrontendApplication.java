package com.holgerhees.indoorpos.frontend;

import com.holgerhees.indoorpos.frontend.controller.samples.SamplesServerEndPoint;
import com.holgerhees.shared.util.ProfileBasedPropertyPlaceholderConfigurer;
import com.holgerhees.shared.web.Application;
import com.holgerhees.shared.web.Router;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.servlet.ServletContext;
import java.net.URL;

public class FrontendApplication implements Application
{
    private static Log LOGGER = LogFactory.getLog( FrontendApplication.class );

    private ApplicationContext applicationContext;
    //private ApplicationConfig applicationConfig;
    private FrontendRouter router;

    @Override
    public ApplicationContext initialize( ServletContext servletContext )
    {
        final long start = System.currentTimeMillis();
        try
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL url = cl.getResource( ProfileBasedPropertyPlaceholderConfigurer.replaceName( "com/holgerhees/indoorpos/config/application.properties" ) );
            PropertyConfigurator.configure( url );

            applicationContext = new ClassPathXmlApplicationContext( new String[]{ "com/holgerhees/indoorpos/FrontendApplicationContext.xml" } );

            //ClasspathSetupHelper classpathSetupHelper = AppContext.getBeanByClass(ClasspathSetupHelper.class);
            //classpathSetupHelper.setup(servletContext.getRealPath("."), DevClasspathDirectory.getFrontendSet());

            router = (FrontendRouter) applicationContext.getBean( "frontendRouter" );

            //javax.websocket.server.ServerContainer serverContainer = (javax.websocket.server.ServerContainer) servletContext.getAttribute( "javax.websocket.server.ServerContainer");
            //serverContainer.addEndpoint(SamplesServerEndPoint.class);

            //applicationConfig = (ApplicationConfig) applicationContext.getBean("applicationConfig");
        } catch( Exception e )
        {

            e.printStackTrace();
            System.out.format( "%n%n###########################################################################################%n" );
            System.out.format( "#################  STOPPED TOMCAT AS FRONTEND STARTUP FAILED WITH EXCEPTION  #############%n" );
            System.out.format( "###########################################################################################%n" );

            System.exit( 1 );
        }

        System.out.println( "#### " + ( System.currentTimeMillis() - start ) + " ms : final System.gc() done ####" );

        System.out.format( "##########################################%n" );
        System.out.format( "#### frontend started in %d seconds   ####%n", Long.valueOf( ( System.currentTimeMillis() - start ) / 1000 ) );
        System.out.format( "##########################################%n" );

        return applicationContext;
    }

    @Override
    public Router getRouter()
    {
        return router;
    }

    @Override
    public void shutdown()
    {
        LOGGER.info( "Shutdown ThreadPoolTaskExecutor and ThreadPoolTaskScheduler." );

        ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler) applicationContext.getBean( "taskScheduler" );
        scheduler.shutdown();

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) applicationContext.getBean( "taskExecutor" );
        executor.shutdown();
    }
}
