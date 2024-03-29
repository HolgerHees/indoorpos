package com.holgerhees.indoorpos.maintainance;

import com.holgerhees.shared.persistance.MaintainanceService;
import com.holgerhees.shared.util.ProfileBasedPropertyPlaceholderConfigurer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.URL;

public class RefreshDatabaseSchema
{
    private static boolean DROP_TABLES = false;

    private RefreshDatabaseSchema()
    {
    }

    public static void main( String[] args )
    {

        BasicConfigurator.resetConfiguration();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource( ProfileBasedPropertyPlaceholderConfigurer.replaceName( "com/holgerhees/indoorpos/config/application.properties" ) );
        PropertyConfigurator.configure( url );

        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext( "DefaultApplicationContext.xml" );
        try
        {
            MaintainanceService maintainanceService = applicationContext.getBean( MaintainanceService.class );
            maintainanceService.createDatabaseSchema( DROP_TABLES );
        } finally
        {
            applicationContext.close();
        }
    }

}
