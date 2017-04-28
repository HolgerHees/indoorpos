package com.holgerhees.indoorpos.maintainance;

import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.holgerhees.indoorpos.persistance.dao.BeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.RoomDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.RoomDTO;
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
			RoomDAO roomDAO = applicationContext.getBean(RoomDAO.class);
			roomDAO.truncate();

			TrackerDAO trackerDAO = applicationContext.getBean(TrackerDAO.class);
			trackerDAO.truncate();

			BeaconDAO beaconDAO = applicationContext.getBean(BeaconDAO.class);
			beaconDAO.truncate();

			// Wohnzimmer
			RoomDTO roomDTO = new RoomDTO();
			roomDTO.setFloor(0);
			roomDTO.setName("Wohnzimmer");
			roomDAO.save( roomDTO );

			// First Floor - 1000 x 736
			TrackerDTO trackerDTO = new TrackerDTO();
			trackerDTO.setUuid("livingroom");
			trackerDTO.setRoomId( roomDTO.getId() );
			trackerDTO.setPosX(953);
			trackerDTO.setPosY(620);
			trackerDTO.setName("Wohnzimmer");
			trackerDAO.save(trackerDTO);

			// Küche
			roomDTO = new RoomDTO();
			roomDTO.setFloor(0);
			roomDTO.setName("Küche");
			roomDAO.save( roomDTO );

			trackerDTO = new TrackerDTO();
			trackerDTO.setUuid("kitchen");
			trackerDTO.setRoomId( roomDTO.getId() );
			trackerDTO.setPosX(295);
			trackerDTO.setPosY(620);
			trackerDTO.setName("Küche");
			trackerDAO.save(trackerDTO);

			// Küche
			/*roomDTO = new RoomDTO();
			roomDTO.setFloor(0);
			roomDTO.setName("HWR");
			roomDAO.save( roomDTO );

			trackerDTO = new TrackerDTO();
			trackerDTO.setUuid("hwr");
			trackerDTO.setRoomId( roomDTO.getId() );
			trackerDTO.setPosX(400);
			trackerDTO.setPosY(280);
			trackerDTO.setName("HWR");
			trackerDAO.save(trackerDTO);*/

			// Flur
			roomDTO = new RoomDTO();
			roomDTO.setFloor(0);
			roomDTO.setName("Flur");
			roomDAO.save( roomDTO );

			trackerDTO = new TrackerDTO();
			trackerDTO.setUuid("floor");
			trackerDTO.setRoomId( roomDTO.getId() );
			trackerDTO.setPosX(715);
			trackerDTO.setPosY(48);
			trackerDTO.setName("Flur");
			trackerDAO.save(trackerDTO);

			// Gästezimmer
			roomDTO = new RoomDTO();
			roomDTO.setFloor(0);
			roomDTO.setName("Gästezimmer");
			roomDAO.save( roomDTO );

			trackerDTO = new TrackerDTO();
			trackerDTO.setUuid("guestroom");
			trackerDTO.setRoomId( roomDTO.getId() );
			trackerDTO.setPosX(953);
			trackerDTO.setPosY(48);
			trackerDTO.setName("Gästezimmer");
			trackerDAO.save(trackerDTO);

			BeaconDTO beaconDTO = new BeaconDTO();
			beaconDTO.setUuid("abcd");
			trackerDTO.setRoomId( roomDTO.getId() );
			beaconDTO.setPosX(100);
			beaconDTO.setPosY(100);
			beaconDTO.setName("Holger");
			beaconDAO.save(beaconDTO);

			//maintainanceService.createDatabaseSchema(DROP_TABLES);
		}
		finally
		{
			applicationContext.close();
		}
	}

}
