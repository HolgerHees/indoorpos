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

			// First Floor - 1000 x 736
			TrackerDTO trackerDTO = new TrackerDTO();
			trackerDTO.setName("Wohnzimmer");
			trackerDTO.setRoom("livingroom");
			trackerDTO.setPosX(980);
			trackerDTO.setPosY(720);
			trackerDAO.save(trackerDTO);

			trackerDTO = new TrackerDTO();
			trackerDTO.setName("Küche");
			trackerDTO.setRoom("kitchen");
			trackerDTO.setPosX(200);
			trackerDTO.setPosY(550);
			trackerDAO.save(trackerDTO);

			trackerDTO = new TrackerDTO();
			trackerDTO.setName("HWR");
			trackerDTO.setRoom("hwr");
			trackerDTO.setPosX(200);
			trackerDTO.setPosY(400);
			trackerDAO.save(trackerDTO);

			trackerDTO = new TrackerDTO();
			trackerDTO.setName("Flur");
			trackerDTO.setRoom("floor");
			trackerDTO.setPosX(400);
			trackerDTO.setPosY(400);
			trackerDAO.save(trackerDTO);

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
