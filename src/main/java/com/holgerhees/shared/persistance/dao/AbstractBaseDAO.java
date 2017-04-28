package com.holgerhees.shared.persistance.dao;

import com.holgerhees.shared.persistance.CustomRowMapper;
import com.holgerhees.shared.persistance.SchemaService;
import com.holgerhees.shared.persistance.dao.helper.JdbcTemplateDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import javax.annotation.PostConstruct;
import java.util.List;

public abstract class AbstractBaseDAO<T>
{

    @Autowired
    protected JdbcTemplateDAO jdbcTemplateDao;

    @Autowired
    protected SchemaService schemaService;

    protected CustomRowMapper<T> customRowMapper = null;

    @PostConstruct
    public void init()
    {
        customRowMapper = new CustomRowMapper<>( getMappedClass(), schemaService );
    }

    public void save( T obj, boolean refreshLastModified )
    {
        customRowMapper.save( jdbcTemplateDao, refreshLastModified, obj );
    }

    public void save( T obj )
    {
        customRowMapper.save( jdbcTemplateDao, true, obj );
    }

    protected List<T> query( String sql )
    {
        return query( sql, new Object[]{}, customRowMapper );
    }

    protected List<T> query( String sql, Object[] args )
    {
        return query( sql, args, customRowMapper );
    }

    protected <T1> List<T1> query( String sql, Object[] args, RowMapper<T1> rowMapper )
    {
        try
        {
            return jdbcTemplateDao.getJdbcTemplate().query( sql, args, rowMapper );
        } catch( EmptyResultDataAccessException e )
        {
            return null;
        }
    }

    protected T queryForObject( String sql, Object[] args )
    {
        try
        {
            return jdbcTemplateDao.getJdbcTemplate().queryForObject( sql, args, customRowMapper );
        } catch( EmptyResultDataAccessException e )
        {
            return null;
        }
    }

    protected <T1> T1 queryForObject( String sql, Object[] args, Class<T1> requiredType )
    {
        try
        {
            return jdbcTemplateDao.getJdbcTemplate().queryForObject( sql, args, requiredType );
        } catch( EmptyResultDataAccessException e )
        {
            return null;
        }
    }

    protected boolean update( String sql, Object... args )
    {
        try
        {
            jdbcTemplateDao.getJdbcTemplate().update( sql, args );
            return true;
        } catch( EmptyResultDataAccessException e )
        {
            return false;
        }
    }

    abstract protected Class<T> getMappedClass();
}
