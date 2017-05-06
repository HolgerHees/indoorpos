package com.holgerhees.indoorpos.maintainance;

import com.holgerhees.indoorpos.persistance.dao.AreaDAO;
import com.holgerhees.indoorpos.persistance.dao.BeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.RoomDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.AreaDTO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
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

            trackerDAO.truncate();
            beaconDAO.truncate();
            roomDAO.truncate();
            areaDAO.truncate();

            // Wohnzimmer
            RoomDTO roomDTO = new RoomDTO();
            roomDTO.setFloor( 0 );
            roomDTO.setName( "Wohnzimmer" );
            roomDAO.save( roomDTO );

            AreaDTO areaDTO = new AreaDTO();
            areaDTO.setRoomId( roomDTO.getId() );
            areaDTO.setTopLeftX( 515 );
            areaDTO.setTopLeftY( 709 );
            areaDTO.setBottomRightX( 734 );
            areaDTO.setBottomRightY( 640 );
            areaDAO.save( areaDTO );

            areaDTO = new AreaDTO();
            areaDTO.setRoomId( roomDTO.getId() );
            areaDTO.setTopLeftX( 510 );
            areaDTO.setTopLeftY( 640 );
            areaDTO.setBottomRightX( 972 );
            areaDTO.setBottomRightY( 280 );
            areaDAO.save( areaDTO );

            // First Floor - 1000 x 736
            TrackerDTO trackerDTO = new TrackerDTO();
            trackerDTO.setUuid( "livingroom" );
            trackerDTO.setRoomId( roomDTO.getId() );
            trackerDTO.setPosX( 953 );
            trackerDTO.setPosY( 620 );
            trackerDTO.setName( "Wohnzimmer" );
            trackerDTO.setIp( "192.168.0.125" );
            trackerDTO.setRssiOffset( 0 );
            trackerDAO.save( trackerDTO );

            // Küche
            roomDTO = new RoomDTO();
            roomDTO.setFloor( 0 );
            roomDTO.setName( "Küche" );
            roomDAO.save( roomDTO );

            areaDTO = new AreaDTO();
            areaDTO.setRoomId( roomDTO.getId() );
            areaDTO.setTopLeftX( 274 );
            areaDTO.setTopLeftY( 640 );
            areaDTO.setBottomRightX( 509 );
            areaDTO.setBottomRightY( 420 );
            areaDAO.save( areaDTO );

            trackerDTO = new TrackerDTO();
            trackerDTO.setUuid( "kitchen" );
            trackerDTO.setRoomId( roomDTO.getId() );
            trackerDTO.setPosX( 295 );
            trackerDTO.setPosY( 620 );
            trackerDTO.setName( "Küche" );
            trackerDTO.setIp( "192.168.0.126" );
            trackerDTO.setRssiOffset( -2 );
            trackerDAO.save( trackerDTO );

            // HWR
            /*roomDTO = new RoomDTO();
            roomDTO.setFloor(0);
			roomDTO.setName("HWR");
			roomDAO.save( roomDTO );

			areaDTO = new AreaDTO();
			areaDTO.setRoomId(roomDTO.getId());
			areaDTO.setTopLeftX(274);
			areaDTO.setTopLeftY(300);
			areaDTO.setBottomRightX(498);
			areaDTO.setBottomRightY(139);
			areaDAO.save(areaDTO);

			trackerDTO = new TrackerDTO();
			trackerDTO.setUuid("hwr");
			trackerDTO.setRoomId( roomDTO.getId() );
			trackerDTO.setPosX(400);
			trackerDTO.setPosY(280);
			trackerDTO.setName("HWR");
            trackerDTO.setIp( "192.168.0.129" );
            trackerDTO.setTxPower( 0 );
			trackerDAO.save(trackerDTO);*/

            // Flur
            roomDTO = new RoomDTO();
            roomDTO.setFloor( 0 );
            roomDTO.setName( "Flur" );
            roomDAO.save( roomDTO );

            areaDTO = new AreaDTO();
            areaDTO.setRoomId( roomDTO.getId() );
            areaDTO.setTopLeftX( 514 );
            areaDTO.setTopLeftY( 265 );
            areaDTO.setBottomRightX( 733 );
            areaDTO.setBottomRightY( 29 );
            areaDAO.save( areaDTO );

            trackerDTO = new TrackerDTO();
            trackerDTO.setUuid( "floor" );
            trackerDTO.setRoomId( roomDTO.getId() );
            trackerDTO.setPosX( 715 );
            trackerDTO.setPosY( 48 );
            trackerDTO.setName( "Flur" );
            trackerDTO.setIp( "192.168.0.127" );
            trackerDTO.setRssiOffset( 0 );
            trackerDAO.save( trackerDTO );

            // Gästezimmer
            roomDTO = new RoomDTO();
            roomDTO.setFloor( 0 );
            roomDTO.setName( "Gästezimmer" );
            roomDAO.save( roomDTO );

            areaDTO = new AreaDTO();
            areaDTO.setRoomId( roomDTO.getId() );
            areaDTO.setTopLeftX( 749 );
            areaDTO.setTopLeftY( 265 );
            areaDTO.setBottomRightX( 973 );
            areaDTO.setBottomRightY( 29 );
            areaDAO.save( areaDTO );

            trackerDTO = new TrackerDTO();
            trackerDTO.setUuid( "guestroom" );
            trackerDTO.setRoomId( roomDTO.getId() );
            trackerDTO.setPosX( 953 );
            trackerDTO.setPosY( 48 );
            trackerDTO.setName( "Gästezimmer" );
            trackerDTO.setIp( "192.168.0.128" );
            trackerDTO.setRssiOffset( 0 );
            trackerDAO.save( trackerDTO );

            // Holgers Phone
            BeaconDTO beaconDTO = new BeaconDTO();
            beaconDTO.setUuid( "c45927d1606f4242b273c52a294489a6" );
            beaconDTO.setName( "Holger" );
            beaconDAO.save( beaconDTO );

            //maintainanceService.createDatabaseSchema(DROP_TABLES);
        } finally
        {
            applicationContext.close();
        }
    }

}
