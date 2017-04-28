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

    public AreaDTO getAreaById(Long areaId)
    {
        return queryForObject("SELECT * FROM area WHERE id = ?", new Object[]{areaId});
    }

    public boolean delete(Long areaId)
    {
        return update("DELETE FROM area WHERE id = ?", areaId);
    }

    public boolean truncate()
    {
        update("DELETE FROM area");
        update("ALTER TABLE area AUTO_INCREMENT = 1");
        return true;
    }

    public List<AreaDTO> getAreas()
    {
        return query("SELECT * FROM area");
    }
}
