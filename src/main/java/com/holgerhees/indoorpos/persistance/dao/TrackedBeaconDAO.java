package com.holgerhees.indoorpos.persistance.dao;

import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;
import com.holgerhees.shared.persistance.dao.AbstractBaseDAO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
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

    public boolean delete( Long trackerId, Long beaconId )
    {
        return update( "DELETE FROM tracked_beacon WHERE tracker_id = ? AND beacon_id = ?", trackerId, beaconId );
    }

    public List<TrackedBeaconDTO> getTrackedBeacons( Long beaconId )
    {
        return query( "SELECT * FROM tracked_beacon WHERE beacon_id = ?", new Object[]{ beaconId } );
    }

    public List<TrackedBeaconDTO> getTrackedBeacons()
    {
        return query( "SELECT * FROM tracked_beacon" );
    }

    public List<Long> getTrackedBeaconIds( Long trackerId)
    {
        return query( "SELECT beacon_id FROM tracked_beacon WHERE tracker_id = ?", new Object[]{ trackerId }, new RowMapper<Long>()
        {
            @Override
            public Long mapRow( ResultSet resultSet, int i ) throws SQLException
            {
                return resultSet.getLong( "beacon_id" );
            }
        } );
    }
}
