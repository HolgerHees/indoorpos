package com.holgerhees.indoorpos.persistance.dao;

import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.shared.persistance.dao.AbstractBaseDAO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component( "trackerDAO" )
public class TrackerDAO extends AbstractBaseDAO<TrackerDTO>
{
    protected Class<TrackerDTO> getMappedClass()
    {
        return TrackerDTO.class;
    }

    public TrackerDTO getTrackerById(Long trackerId)
    {
        return queryForObject("SELECT * FROM tracker WHERE id = ?", new Object[]{trackerId});
    }

    public boolean delete(Long trackerId)
    {
        return update("DELETE FROM tracker WHERE id = ?", trackerId);
    }

    public boolean truncate()
    {
        update("DELETE FROM tracker");
        update("ALTER TABLE tracker AUTO_INCREMENT = 1");
        return true;
    }

    public TrackerDTO getTrackerByUUID(String uuid)
    {
        return queryForObject("SELECT * FROM tracker WHERE uuid = ?", new Object[]{uuid});
    }

    public Map<Long, TrackerDTO> getTrackerIDMap()
    {
        List<TrackerDTO> tracker = query("SELECT * FROM tracker");

        Map<Long, TrackerDTO> trackerMap = new HashMap<>();
        for( TrackerDTO trackerDTO : tracker )
        {
            trackerMap.put(trackerDTO.getId(), trackerDTO);
        }

        return trackerMap;
    }

    public List<TrackerDTO> getTracker()
    {
        return query("SELECT * FROM tracker");
    }
}
