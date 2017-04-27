package com.holgerhees.indoorpos.maintainance;

import java.net.URL;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.holgerhees.persistance.MaintainanceService;
import com.holgerhees.persistance.SchemaService;
import com.holgerhees.persistance.dao.helper.SchemaDAO;
import com.holgerhees.persistance.schema.Table;
import com.holgerhees.indoorpos.service.util.ProfileBasedPropertyPlaceholderConfigurer;

public class ShowDatabaseSchema {
	
	public static void main(String[] args) {
		
		BasicConfigurator.resetConfiguration();

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL url = cl.getResource(ProfileBasedPropertyPlaceholderConfigurer.replaceName( "com/holgerhees/indoorpos/config/application.properties"));
		PropertyConfigurator.configure(url);

		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("DefaultApplicationContext.xml");
		try
		{
			MaintainanceService maintainanceService = applicationContext.getBean(MaintainanceService.class);
			maintainanceService.showDatabaseSchema();
		}
		finally
		{
			applicationContext.close();
		}
	}

}
