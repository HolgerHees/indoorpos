package com.holgerhees.indoorpos.frontend.controller.tracker;

import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.indoorpos.frontend.service.CacheService;
import com.holgerhees.indoorpos.persistance.dao.BeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackedBeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.util.GSonFactory;
import com.holgerhees.shared.web.view.TextView;
import com.holgerhees.shared.web.view.View;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@Component( "trackerController" )
public class TrackerController implements Controller
{
    private static Log LOGGER = LogFactory.getLog( TrackerController.class );

    private class TrackedBeacon
    {
        private String uuid;
        private int txpower;
        private int rssi;
        private int samples;
    }

    private class Parameter
    {
        private String uuid;
        private List<TrackedBeacon> trackedBeacons;
    }

    @Autowired
    private BeaconDAO beaconDAO;

    @Autowired
    private TrackerDAO trackerDAO;

    @Autowired
    private TrackedBeaconDAO trackedBeaconDAO;

    @Autowired
    private CacheService cacheService;

    @Override
    final public View handle( Request req )
    {
        byte[] body = null;

        try
        {
            InputStream reader = req.getHttpRequest().getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buf[] = new byte[1024];
            int count;
            while( ( count = reader.read( buf ) ) > 0 )
            {
                baos.write( buf, 0, count );
            }
            body = baos.toByteArray();
        } catch( IOException e )
        {

        }

        if( body == null || body.length == 0 )
        {
            return new TextView( req, "empty request" );
        }

        String json = new String( body, Charset.defaultCharset() );

        Parameter param = GSonFactory.createGSon().fromJson( json, Parameter.class );

        Map<String, BeaconDTO> beaconDTOMap = beaconDAO.getBeaconUUIDMap();
        TrackerDTO trackerDTO = trackerDAO.getTrackerByUUID( param.uuid );

        if( trackerDTO == null )
        {
            LOGGER.info( "Skip unknown tracker with 'uuid': " + param.uuid );
        }
        else
        {
            boolean found = false;

            List<Long> activeTrackedBeaconIds = trackedBeaconDAO.getActiveTrackedBeaconIds( trackerDTO.getId() );

            for( TrackedBeacon beacon : param.trackedBeacons )
            {
                BeaconDTO beaconDTO = beaconDTOMap.get( beacon.uuid );

                if( beaconDTO == null )
                {
                    //LOGGER.info( "Skip unknown beacon with 'uuid': " + beacon.uuid );
                    continue;
                }

                if( !activeTrackedBeaconIds.contains( beaconDTO.getId() ) )
                {
                    if( beacon.samples <= CacheService.MIN_SAMPLES )
                    {
                        LOGGER.info( "Skip beacon with 'uuid': " + beacon.uuid + ". Low samples." );
                        continue;
                    }
                }

                /*if( beacon.rssi <= CacheService.MIN_RSSI )
                {
                    LOGGER.info( "Skip known beacon with 'uuid': " + beacon.uuid + ". Low rssi." );
                    continue;
                }*/

                LOGGER.info("Track " + trackerDTO.getName() + " " + beacon.rssi + " (" + beacon.samples + ")" );

                TrackedBeaconDTO trackedBeaconDTO = new TrackedBeaconDTO();
                trackedBeaconDTO.setTrackerId( trackerDTO.getId() );
                trackedBeaconDTO.setBeaconId( beaconDTO.getId() );
                trackedBeaconDTO.setTxPower( beacon.txpower );
                trackedBeaconDTO.setRssi( beacon.rssi );
                trackedBeaconDTO.setSamples(  beacon.samples );

                trackedBeaconDAO.save( trackedBeaconDTO );

                found = true;
            }

            if( found )
            {
                cacheService.trackerUdate( trackerDTO );
            }
        }

        return new TextView( req, "tracked" );
    }
}
