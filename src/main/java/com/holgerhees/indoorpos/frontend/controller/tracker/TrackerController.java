package com.holgerhees.indoorpos.frontend.controller.tracker;

import com.holgerhees.indoorpos.frontend.controller.Controller;
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
    private class TrackedBeacon
    {
        private String uuid;
        private int txPower;
        private int rssi;
    }

    private class Parameter
    {
        private String uuid;
        private List<TrackedBeacon> trachedBeacon;
    }

    @Autowired
    private BeaconDAO beaconDAO;

    @Autowired
    private TrackerDAO trackerDAO;

    @Autowired
    private TrackedBeaconDAO trackedBeaconDAO;

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

        for( TrackedBeacon beacon : param.trachedBeacon )
        {
            BeaconDTO beaconDTO = beaconDTOMap.get( beacon.uuid );

            TrackedBeaconDTO trackedBeaconDTO = new TrackedBeaconDTO();
            trackedBeaconDTO.setTrackerId( trackerDTO.getId() );
            trackedBeaconDTO.setBeaconId( beaconDTO.getId() );
            trackedBeaconDTO.setTxPower( beacon.txPower );
            trackedBeaconDTO.setRssi( beacon.rssi );

            beaconDAO.save( beaconDTO );
        }

        return new TextView( req, "tracked" );
    }
}
