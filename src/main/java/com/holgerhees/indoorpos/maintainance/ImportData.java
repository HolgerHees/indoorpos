package com.holgerhees.indoorpos.maintainance;

import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.holgerhees.indoorpos.persistance.dao.BeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.shared.util.ProfileBasedPropertyPlaceholderConfigurer;

public class ImportData
{
	private ImportData()
	{
	}

	public static void main(String[] args)
	{

		BasicConfigurator.resetConfiguration();

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL url = cl.getResource(ProfileBasedPropertyPlaceholderConfigurer.replaceName("com/holgerhees/indoorpos/config/application.properties"));
		PropertyConfigurator.configure(url);

		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
			"com/holgerhees/indoorpos/MaintainanceApplicationContext.xml");
		try
		{
			BeaconDAO beaconDAO = applicationContext.getBean(BeaconDAO.class);
			beaconDAO.truncate();

			TrackerDAO trackerDAO = applicationContext.getBean(TrackerDAO.class);
			trackerDAO.truncate();

			String[] tracker = new String[] { "Livingroom", "Kitchen", "Guestroom", "Floor" };
			for( String name : tracker )
			{
				TrackerDTO trackerDTO = new TrackerDTO();
				trackerDTO.setName(name);
				trackerDTO.setRoom(name);
				trackerDAO.save(trackerDTO);
			}

			BeaconDTO beaconDTO = new BeaconDTO();
			beaconDTO.setName("Holger");
			beaconDTO.setUuid("abcd");
			beaconDAO.save(beaconDTO);

			//maintainanceService.createDatabaseSchema(DROP_TABLES);
		}
		finally
		{
			applicationContext.close();
		}
	}

}
