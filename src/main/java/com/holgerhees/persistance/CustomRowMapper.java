package com.holgerhees.persistance;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import com.holgerhees.persistance.dao.helper.JdbcTemplateDAO;
import com.holgerhees.persistance.dto.AbstractBaseDTO;
import com.holgerhees.persistance.schema.Column;
import com.holgerhees.persistance.schema.Table;

public class CustomRowMapper<T> implements RowMapper<T>
{
	private Class<T> mappedClass;

	private Table table;

	public CustomRowMapper(Class<T> mappedClass, SchemaService schemaService)
	{
		this.mappedClass = mappedClass;
		this.table = schemaService.getTable(mappedClass);
	}

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException
	{

		try
		{

			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			T mappedObject = this.mappedClass.newInstance();

			for( int index = 1; index <= columnCount; index++ )
			{
				String columnName = metaData.getColumnName(index);

				Column column = table.getColumn(columnName);

				// is unmapped field
				if( column == null )
				{ continue; }
				
				/*System.out.println("0:" + columnName);
				System.out.println("1:" + column);
				System.out.println("2:" + table.getName());
				System.out.println("3:" + column.getName());
				System.out.println("4:" + column.getType());*/

				Object value = JdbcUtils.getResultSetValue(rs, index, column.getType());

				if( column.getSetterConverter() != null )
				{
					value = column.getSetterConverter().invoke(null, value);
				}

				column.getSetter().invoke(mappedObject, value);
			}

			return mappedObject;
		}
		catch( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
		{
			e.printStackTrace();
		}

		return null;
	}

	public T save(JdbcTemplateDAO jdbcTemplateDao, T dto)
	{
		return save(jdbcTemplateDao, true, dto);
	}

	public T save(JdbcTemplateDAO jdbcTemplateDao, boolean refreshLastModified, T dto)
	{

		try
		{
			if( dto instanceof AbstractBaseDTO )
			{
				Date now = new Date((System.currentTimeMillis() / 1000L) * 1000L);
				if( ((AbstractBaseDTO) dto).getCreated() == null )
				{
					((AbstractBaseDTO) dto).setCreated(now);
					((AbstractBaseDTO) dto).setLastModified(now);
				}
				else if( refreshLastModified )
				{
					((AbstractBaseDTO) dto).setLastModified(now);
				}
			}

			Column[] primaryColumns = table.getPrimaryColumns();
			Object[] primaryValues = new Object[primaryColumns.length];
			for( int i = 0; i < primaryValues.length; i++ )
			{
				primaryValues[i] = primaryColumns[i].getGetter().invoke(dto);

				if( primaryColumns[i].getGetterConverter() != null )
				{ primaryValues[i] = primaryColumns[i].getGetterConverter().invoke(primaryValues[i]); }
			}

			// PRIMARY KEY TABLES
			if( primaryColumns.length == 1 && !primaryColumns[0].isInsertable() )
			{
				boolean isInsert = primaryValues[0] == null;

				Map<String, Object> mappedValueFields = new HashMap<>();

				for( Column column : table.getColumns() )
				{
					Object value = column.getGetter().invoke(dto);

					if( column.getGetterConverter() != null )
					{ value = column.getGetterConverter().invoke(value); }

					if( (!isInsert && !column.isUpdateable()) || (isInsert && !column.isInsertable()) )
					{ continue; }

					mappedValueFields.put(column.getName(), value);
				}

				String fieldSQL = StringUtils.join(mappedValueFields.keySet(), " = ?, ") + " = ? ";

				if( isInsert )
				{
					jdbcTemplateDao.getJdbcTemplate().update("INSERT INTO " + table.getName() + " SET " + fieldSQL,
						new ArgumentPreparedStatementSetter(mappedValueFields.values().toArray()));

					Object id = jdbcTemplateDao.getJdbcTemplate().queryForObject("select last_insert_id()", primaryColumns[0].getType());
					primaryColumns[0].getSetter().invoke(dto, id);
				}

				else
				{
					List<Object> values = new LinkedList<>();
					values.addAll(mappedValueFields.values());
					values.add(primaryValues[0]);

					jdbcTemplateDao.getJdbcTemplate()
						.update("UPDATE " + table.getName() + " SET " + fieldSQL + " WHERE " + primaryColumns[0].getName() + " = ? ",
							new ArgumentPreparedStatementSetter(values.toArray()));
				}
			}
			else
			{
				Map<String, Object> mappedInsertValueFields = new HashMap<>();
				Map<String, Object> mappedUpdateValueFields = new HashMap<>();

				for( Column column : table.getColumns() )
				{
					Object value = column.getGetter().invoke(dto);

					if( column.getGetterConverter() != null )
					{ value = column.getGetterConverter().invoke(value); }

					if( column.isInsertable() )
					{ mappedInsertValueFields.put(column.getName(), value); }

					if( column.isUpdateable() )
					{ mappedUpdateValueFields.put(column.getName(), value); }
				}

				String insertFieldSQL = StringUtils.join(mappedInsertValueFields.keySet(), " = ?, ") + " = ? ";
				String updateFieldSQL = StringUtils.join(mappedUpdateValueFields.keySet(), " = ?, ") + " = ? ";

				Object[] insertValues = mappedInsertValueFields.values().toArray();
				Object[] updateValues = mappedUpdateValueFields.values().toArray();

				Object[] values = new Object[insertValues.length + updateValues.length];
				System.arraycopy(insertValues, 0, values, 0, insertValues.length);
				System.arraycopy(updateValues, 0, values, insertValues.length, updateValues.length);
				
				/*LOGGER.error("INSERT INTO " + table.getName() + " SET " + insertFieldSQL + " ON DUPLICATE KEY UPDATE " + updateFieldSQL);
				for( Object obj: values )
				{
					LOGGER.error(obj);
				}*/

				jdbcTemplateDao.getJdbcTemplate()
					.update("INSERT INTO " + table.getName() + " SET " + insertFieldSQL + " ON DUPLICATE KEY UPDATE " + updateFieldSQL,
						new ArgumentPreparedStatementSetter(values));

				StringBuilder whereFieldSQL = new StringBuilder();
				for( Column column : primaryColumns )
				{
					whereFieldSQL.append(" AND " + column.getName() + " = ? ");
				}

				//LOGGER.error("SELECT * FROM " + table.getName() + " WHERE " + whereFieldSQL.substring(5));
				/*for( Object obj: primaryValues )
				{
					LOGGER.error(obj);
				}*/

				return jdbcTemplateDao.getJdbcTemplate()
					.queryForObject("SELECT * FROM " + table.getName() + " WHERE " + whereFieldSQL.substring(5), primaryValues, this);
			}

		}
		catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
		{
			e.printStackTrace();
		}

		return dto;
	}
}
