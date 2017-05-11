package com.holgerhees.indoorpos.maintainance;

import com.holgerhees.indoorpos.persistance.dao.AreaDAO;
import com.holgerhees.indoorpos.persistance.dao.BeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.CloseRoomDAO;
import com.holgerhees.indoorpos.persistance.dao.RoomDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.AreaDTO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.CloseRoomDTO;
import com.holgerhees.indoorpos.persistance.dto.RoomDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.shared.util.ProfileBasedPropertyPlaceholderConfigurer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.URL;

public class ImportData
{
    private ImportData()
    {
    }

    public static void main( String[] args )
    {

        BasicConfigurator.resetConfiguration();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource( ProfileBasedPropertyPlaceholderConfigurer.replaceName( "com/holgerhees/indoorpos/config/application.properties" ) );
        PropertyConfigurator.configure( url );

        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "com/holgerhees/indoorpos/MaintainanceApplicationContext.xml" );
        try
        {
            RoomDAO roomDAO = applicationContext.getBean( RoomDAO.class );
            AreaDAO areaDAO = applicationContext.getBean( AreaDAO.class );
            TrackerDAO trackerDAO = applicationContext.getBean( TrackerDAO.class );
            BeaconDAO beaconDAO = applicationContext.getBean( BeaconDAO.class );
	        CloseRoomDAO closeRoomDAO = applicationContext.getBean( CloseRoomDAO.class);

	        closeRoomDAO.truncate();
            trackerDAO.truncate();
            beaconDAO.truncate();
            roomDAO.truncate();
            areaDAO.truncate();

	        // First Floor - 1000 x 736

	        // strongSignalRssiThreshold
	        // - A signal stronger then XX, always skip priorised check. Means the new tracker will be active.
	        // - A signal stronger then XX will always skip the closeRoom check

	        // priorisedRssiOffset
	        // - A new tracker must be higher then XX to beat a priorised tracker

            // Wohnzimmer
            RoomDTO livingroomDTO = createRoom( roomDAO, "Wohnzimmer", 0 );
            createArea( areaDAO, livingroomDTO, 515, 709, 734, 640 );
	        createArea( areaDAO, livingroomDTO, 510, 640, 972, 280 );
			createTracker( trackerDAO, livingroomDTO, "livingroom", "Wohnzimmer", "192.168.0.125", -68, 10, 953, 620 );

            // Küche
	        RoomDTO kitchenDTO = createRoom( roomDAO, "Küche", 0 );
	        createArea( areaDAO, kitchenDTO, 274, 640, 509, 420 );
	        createTracker( trackerDAO, kitchenDTO, "kitchen", "Küche", "192.168.0.126", -65, 10, 295, 620 );

	        // HWR
	        //RoomDTO hwrDTO = createRoom( roomDAO, "HWR", 0 );
	        //createArea( areaDAO, hwrDTO, 274, 300, 498, 139 );
	        //createTracker( trackerDAO, hwrDTO, "hwr", "HWR", "192.168.0.129", -68, 10, 400, 280 );

	        // Flur
	        RoomDTO floorDTO = createRoom( roomDAO, "Flur", 0 );
	        createArea( areaDAO, floorDTO, 514, 265, 733, 29 );
	        createTracker( trackerDAO, floorDTO, "floor", "Flur", "192.168.0.127", -65, 10, 715, 48 );

	        // Gästezimmer
	        RoomDTO guestroomDTO = createRoom( roomDAO, "Gästezimmer", 0 );
	        createArea( areaDAO, guestroomDTO, 749, 265, 973, 29 );
	        createTracker( trackerDAO, guestroomDTO, "guestroom", "Gästezimmer", "192.168.0.128", -68, 10, 953, 48 );

	        // Close room relations
	        attachCloseRoom( closeRoomDAO, livingroomDTO, kitchenDTO );
	        attachCloseRoom( closeRoomDAO, livingroomDTO, floorDTO );

	        attachCloseRoom( closeRoomDAO, kitchenDTO, livingroomDTO );

	        attachCloseRoom( closeRoomDAO, floorDTO, livingroomDTO );
	        attachCloseRoom( closeRoomDAO, floorDTO, guestroomDTO );

	        attachCloseRoom( closeRoomDAO, guestroomDTO, floorDTO );


			// Holgers Phone
            createPhone( beaconDAO, "c45927d1606f4242b273c52a294489a6", "Holger" );

            //maintainanceService.createDatabaseSchema(DROP_TABLES);
        } finally
        {
            applicationContext.close();
        }
    }

	private static void attachCloseRoom(CloseRoomDAO closeRoomDAO, RoomDTO roomDTO, RoomDTO _roomDTO )
	{
		CloseRoomDTO closeRoomDTO = new CloseRoomDTO();
		closeRoomDTO.setRoomId( roomDTO.getId() );
		closeRoomDTO.setCloseRoomId( _roomDTO.getId() );
		closeRoomDAO.save( closeRoomDTO );
	}

	private static void createPhone( BeaconDAO beaconDAO, String uuid, String name )
	{
		// Holgers Phone
		BeaconDTO beaconDTO = new BeaconDTO();
		beaconDTO.setUuid( uuid );
		beaconDTO.setName( name );
		beaconDAO.save( beaconDTO );

	}

	private static void createTracker(TrackerDAO trackerDAO, RoomDTO roomDTO, String uuid, String name, String ip, int strongSignalRssiThreshold, int priorisedRssiOffset, int posX, int posY )
	{
		TrackerDTO trackerDTO = new TrackerDTO();
		trackerDTO.setUuid( uuid );
		trackerDTO.setRoomId( roomDTO.getId() );
		trackerDTO.setPosX( posX );
		trackerDTO.setPosY( posY );
		trackerDTO.setName( name );
		trackerDTO.setIp( ip );
		trackerDTO.setStrongSignalRssiThreshold( strongSignalRssiThreshold );
		trackerDTO.setPriorisedRssiOffset( priorisedRssiOffset );
		trackerDAO.save( trackerDTO );
	}

	private static void createArea(AreaDAO areaDAO, RoomDTO roomDTO, int topLeftX, int topLeftY1, int bottomRightX, int bottomRightY )
	{
		AreaDTO areaDTO = new AreaDTO();
		areaDTO.setRoomId( roomDTO.getId() );
		areaDTO.setTopLeftX( topLeftX );
		areaDTO.setTopLeftY( topLeftY1 );
		areaDTO.setBottomRightX( bottomRightX );
		areaDTO.setBottomRightY( bottomRightY );
		areaDAO.save( areaDTO );
	}

	private static RoomDTO createRoom( RoomDAO roomDAO, String name, int floor )
    {
	    RoomDTO roomDTO = new RoomDTO();
	    roomDTO.setFloor( 0 );
	    roomDTO.setName( "Gästezimmer" );
	    roomDAO.save( roomDTO );

	    return roomDTO;
    }
}
