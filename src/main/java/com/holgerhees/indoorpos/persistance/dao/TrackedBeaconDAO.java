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

    public TrackedBeaconDTO getTrackedBeaconById(Long trackedBeaconId)
    {
        return queryForObject("SELECT * FROM tracked_beacon WHERE id = ?", new Object[]{trackedBeaconId});
    }

    public boolean delete(Long trackedBeaconId)
    {
        return update("DELETE FROM tracked_beacon WHERE id = ?", trackedBeaconId);
    }

    public List<TrackedBeaconDTO> getTrackedBeacons()
    {
        Date date = new Date();
        date.setTime(date.getTime() - 1000 * 2);

        return query("SELECT * FROM tracked_beacon ORDER BY created DESC");
    }
}
