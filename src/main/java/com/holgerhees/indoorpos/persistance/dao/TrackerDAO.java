package com.holgerhees.indoorpos.persistance.dao;

import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.shared.persistance.dao.AbstractBaseDAO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component( "trackerDAO" )
public class TrackerDAO extends AbstractBaseDAO<TrackerDTO>
{
    protected Class<TrackerDTO> getMappedClass()
    {
        return TrackerDTO.class;
    }

    public boolean delete( Long trackerId )
    {
        return update( "DELETE FROM tracker WHERE id = ?", trackerId );
    }

    public boolean truncate()
    {
        update( "DELETE FROM tracker" );
        update( "ALTER TABLE tracker AUTO_INCREMENT = 1" );
        return true;
    }

    public List<TrackerDTO> getTracker()
    {
        return query( "SELECT * FROM tracker" );
    }
}
