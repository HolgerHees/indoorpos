package com.holgerhees.indoorpos.persistance.dao;

import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.shared.persistance.dao.AbstractBaseDAO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component( "beaconDAO" )
public class BeaconDAO extends AbstractBaseDAO<BeaconDTO>
{
    protected Class<BeaconDTO> getMappedClass()
    {
        return BeaconDTO.class;
    }

    public boolean truncate()
    {
        update( "DELETE FROM beacon" );
        update( "ALTER TABLE beacon AUTO_INCREMENT = 1" );
        return true;
    }

    public List<BeaconDTO> getBeacons()
    {
        return query( "SELECT * FROM beacon" );
    }
}
