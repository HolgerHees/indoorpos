package com.holgerhees.indoorpos.persistance.dao;

import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;
import com.holgerhees.shared.persistance.dao.AbstractBaseDAO;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component( "trackedBeaconDAO" )
public class TrackedBeaconDAO extends AbstractBaseDAO<TrackedBeaconDTO>
{
    protected Class<TrackedBeaconDTO> getMappedClass()
    {
        return TrackedBeaconDTO.class;
    }

    public TrackedBeaconDTO getTrackedBeaconById( Long trackedBeaconId )
    {
        return queryForObject( "SELECT * FROM tracked_beacon WHERE id = ?", new Object[]{ trackedBeaconId } );
    }

    public boolean delete( Long trackedBeaconId )
    {
        return update( "DELETE FROM tracked_beacon WHERE id = ?", trackedBeaconId );
    }

    public List<TrackedBeaconDTO> getActiveTrackedBeacons()
    {
        // 2 x ~3 second interval
        return query( "SELECT * FROM tracked_beacon WHERE lastModified >= DATE_SUB( NOW(), INTERVAL 8 SECOND ) ORDER BY created DESC" );
    }
}
