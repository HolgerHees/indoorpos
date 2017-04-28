package com.holgerhees.shared.persistance.dao.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component( "jdbcTemplate" )
public class JdbcTemplateDAO
{

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource( DataSource dataSource )
    {
        this.jdbcTemplate = new JdbcTemplate( dataSource );
    }

    public JdbcTemplate getJdbcTemplate()
    {
        return jdbcTemplate;
    }
}
