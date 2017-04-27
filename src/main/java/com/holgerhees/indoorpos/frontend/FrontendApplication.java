package com.holgerhees.indoorpos.frontend;

import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.holgerhees.indoorpos.Application;
import com.holgerhees.indoorpos.Router;
import com.holgerhees.util.ProfileBasedPropertyPlaceholderConfigurer;

public class FrontendApplication implements Application
{
	private static Log LOGGER = LogFactory.getLog(FrontendApplication.class);

	private ApplicationContext applicationContext;
	//private ApplicationConfig applicationConfig;
	private FrontendRouter router;

	public ApplicationContext initialize(ServletContext servletContext)
	{

		final long start = System.currentTimeMillis();
		try
		{

			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			URL url = cl.getResource(ProfileBasedPropertyPlaceholderConfigurer.replaceName("com/holgerhees/indoorpos/config/application.properties"));
			PropertyConfigurator.configure(url);

			applicationContext = new ClassPathXmlApplicationContext(new String[] { "com/holgerhees/indoorpos/FrontendApplicationContext.xml" });

			//ClasspathSetupHelper classpathSetupHelper = AppContext.getBeanByClass(ClasspathSetupHelper.class);
			//classpathSetupHelper.setup(servletContext.getRealPath("."), DevClasspathDirectory.getFrontendSet());

			router = (FrontendRouter) applicationContext.getBean("frontendRouter");

			//applicationConfig = (ApplicationConfig) applicationContext.getBean("applicationConfig");
		}
		catch( Exception e )
		{

			e.printStackTrace();
			System.out.format("%n%n###########################################################################################%n");
			System.out.format("#################  STOPPED TOMCAT AS FRONTEND STARTUP FAILED WITH EXCEPTION  #############%n");
			System.out.format("###########################################################################################%n");

			System.exit(1);
		}

		System.out.println("#### " + (System.currentTimeMillis() - start) + " ms : final System.gc() done ####");

		System.out.format("##########################################%n");
		System.out.format("#### frontend started in %d seconds   ####%n", Long.valueOf((System.currentTimeMillis() - start) / 1000));
		System.out.format("##########################################%n");

		return applicationContext;
	}

	public Router getRouter()
	{
		return router;
	}

	public void shutdown()
	{
		LOGGER.info("Shutdown ThreadPoolTaskExecutor and ThreadPoolTaskScheduler.");

		ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler) applicationContext.getBean("taskScheduler");
		scheduler.shutdown();

		ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) applicationContext.getBean("taskExecutor");
		executor.shutdown();
	}
}
