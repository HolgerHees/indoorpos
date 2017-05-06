package com.holgerhees.indoorpos.persistance.dao;

import com.holgerhees.indoorpos.persistance.dto.AreaDTO;
import com.holgerhees.shared.persistance.dao.AbstractBaseDAO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component( "areaDAO" )
public class AreaDAO extends AbstractBaseDAO<AreaDTO>
{
    protected Class<AreaDTO> getMappedClass()
    {
        return AreaDTO.class;
    }

    public boolean truncate()
    {
        update( "DELETE FROM area" );
        update( "ALTER TABLE area AUTO_INCREMENT = 1" );
        return true;
    }

    public List<AreaDTO> getAreas()
    {
        return query( "SELECT * FROM area" );
    }
}
