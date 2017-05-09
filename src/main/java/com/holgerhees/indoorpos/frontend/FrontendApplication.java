package com.holgerhees.indoorpos.frontend;

import com.holgerhees.indoorpos.frontend.service.CacheServiceBuilderJob;
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
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

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
            //serverContainer.addEndpoint(SamplesEndPoint.class);

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
        LOGGER.info( "Shutdown ThreadPoolTaskExecutor, ThreadPoolTaskScheduler and CacheWatcherService." );

        ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler) applicationContext.getBean( "taskScheduler" );
        scheduler.shutdown();

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) applicationContext.getBean( "taskExecutor" );
        executor.shutdown();

        CacheServiceBuilderJob watcher = (CacheServiceBuilderJob) applicationContext.getBean( "cacheWatcherService");
        watcher.shutdown();

        shutdownDatabase();
    }

    public void shutdownDatabase()
    {
        LOGGER.info("Calling MySQL AbandonedConnectionCleanupThread shutdown");
        com.mysql.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();

        // Now deregister JDBC drivers in this context's ClassLoader:
        // Get the webapp's ClassLoader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        // Loop through all drivers
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements())
        {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cl)
            {
                // This driver was registered by the webapp's ClassLoader, so deregister it:
                try
                {
                    LOGGER.info("Deregistering JDBC driver {}" + driver);
                    DriverManager.deregisterDriver(driver);
                }
                catch (SQLException ex) {
                    LOGGER.info("Error deregistering JDBC driver {}" + ex);
                }
            }
            else
            {
                // driver was not registered by the webapp's ClassLoader and may be in use elsewhere
                LOGGER.info("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader" + driver);
            }
        }
    }
}
