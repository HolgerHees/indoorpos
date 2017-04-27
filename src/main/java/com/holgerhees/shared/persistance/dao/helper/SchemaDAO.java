package com.holgerhees.shared.persistance.dao.helper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Component;

import com.holgerhees.shared.persistance.SchemaService;
import com.holgerhees.shared.persistance.annotations.DbTable;
import com.holgerhees.shared.persistance.schema.Column;
import com.holgerhees.shared.persistance.schema.Index;
import com.holgerhees.shared.persistance.schema.Table;

@Component( "schemaDAO" )
public class SchemaDAO
{

	private static Log LOGGER = LogFactory.getLog(SchemaDAO.class);

	@Autowired
	JdbcTemplateDAO jdbcTemplateDao;

	public void dropTable(Class<?> mappedClass)
	{
		DbTable table = SchemaService.getTableAnnotation(mappedClass);
		jdbcTemplateDao.getJdbcTemplate().execute("DROP TABLE " + table.name());
	}

	public void dropTableIfExists(Class<?> mappedClass)
	{
		DbTable table = SchemaService.getTableAnnotation(mappedClass);
		jdbcTemplateDao.getJdbcTemplate().execute("DROP TABLE IF EXISTS " + table.name());
	}

	public void optimizeTable(Class<?> mappedClass)
	{
		DbTable table = SchemaService.getTableAnnotation(mappedClass);
		jdbcTemplateDao.getJdbcTemplate().execute("OPTIMIZE TABLE " + table.name());
	}

	public void createConstraint(String table, String constraintDefinition)
	{
		StringBuilder sb = new StringBuilder("ALTER TABLE " + table + " ");
		sb.append("ADD " + constraintDefinition);
		jdbcTemplateDao.getJdbcTemplate().execute(sb.toString());
	}

	public void dropConstraint(String table, String constraintName)
	{
		StringBuilder sb = new StringBuilder("ALTER TABLE " + table + " ");
		sb.append("DROP FOREIGN KEY " + constraintName);
		jdbcTemplateDao.getJdbcTemplate().execute(sb.toString());
	}

	public void createIndex(String table, String indexDefinition)
	{
		StringBuilder sb = new StringBuilder("ALTER TABLE " + table + " ");
		sb.append("ADD " + indexDefinition);
		jdbcTemplateDao.getJdbcTemplate().execute(sb.toString());
	}

	public void dropIndex(String table, String indexName)
	{
		StringBuilder sb = new StringBuilder("ALTER TABLE " + table + " ");
		sb.append("DROP INDEX `" + indexName + "`");
		jdbcTemplateDao.getJdbcTemplate().execute(sb.toString());
	}

	public void createColumn(String table, String columnDefinition, String afterColumn)
	{
		StringBuilder sb = new StringBuilder("ALTER TABLE " + table + " ");
		sb.append("ADD COLUMN " + columnDefinition);
		if( afterColumn != null )
		{ sb.append(" AFTER " + afterColumn); }
		jdbcTemplateDao.getJdbcTemplate().execute(sb.toString());
	}

	public String getCreateTableStatement(Table table, boolean ifNotExists)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE " + (ifNotExists ? "IF NOT EXISTS " : "") + table.getName() + "(\n");

		// Columns
		List<String> definitions = new LinkedList<>();
		for( Column column : table.getColumns() )
		{
			definitions.add(column.getDefinition());
		}
		sb.append("\n\t" + StringUtils.join(definitions, ",\n\t"));

		// Indexes
		definitions = new LinkedList<>();
		for( Index index : table.getIndexes() )
		{
			definitions.add(index.getDefinition());
		}
		if( !definitions.isEmpty() )
		{ sb.append(",\n\t" + StringUtils.join(definitions, ",\n\t")); }

		// Constraints
		definitions = new LinkedList<>();
		for( Column column : table.getColumns() )
		{
			if( column.getConstraint() == null )
			{ continue; }
			definitions.add(column.getConstraint().getDefinition());
		}
		if( !definitions.isEmpty() )
		{ sb.append(",\n\t" + StringUtils.join(definitions, ",\n\t")); }

		sb.append("\n) ENGINE=INNODB");

		return sb.toString();
	}

	public void createTable(Table table, boolean ifNotExists)
	{
		jdbcTemplateDao.getJdbcTemplate().execute(getCreateTableStatement(table, ifNotExists));
	}

	public List<String> getExistingTables()
	{
		String sql = "SHOW TABLES";
		List<String> existingTableNames = jdbcTemplateDao.getJdbcTemplate().query(sql, new RowMapperResultSetExtractor<String>(new RowMapper<String>()
		{
			@Override
			public String mapRow(ResultSet rs, int arg1) throws SQLException
			{
				return rs.getString(1);
			}
		}));
		return existingTableNames;
	}

	public Map<String, String> getExistingColumns(String tableName)
	{
		String sql = "SHOW COLUMNS FROM " + tableName;
		List<String[]> existingColumnData = jdbcTemplateDao.getJdbcTemplate()
			.query(sql, new RowMapperResultSetExtractor<String[]>(new RowMapper<String[]>()
			{
				@Override
				public String[] mapRow(ResultSet rs, int arg1) throws SQLException
				{
					StringBuilder builder = new StringBuilder("`" + rs.getString("Field") + "` " + rs.getString("Type") + " ");
					builder.append(rs.getString("Null").equals("NO") ? "NOT " : "");
					builder.append("NULL");
					if( rs.getString("Extra").equals("auto_increment") )
					{
						builder.append(" AUTO_INCREMENT");
					}

					return new String[] { rs.getString("Field"), builder.toString() };
				}
			}));

		Map<String, String> existingColumns = new HashMap<>();
		for( String[] col : existingColumnData )
		{
			existingColumns.put(col[0], col[1]);
		}
		return existingColumns;
	}

	public List<String> getExistingIndexNames(String tableName)
	{
		String sql = "SHOW INDEXES FROM " + tableName;
		List<String> existingIndexNames = jdbcTemplateDao.getJdbcTemplate().query(sql, new RowMapperResultSetExtractor<String>(new RowMapper<String>()
		{
			@Override
			public String mapRow(ResultSet rs, int arg1) throws SQLException
			{
				return rs.getString("Key_name");
			}
		}));
		return new ArrayList<>(new HashSet<String>(existingIndexNames));
	}

	public List<String> getExistingConstraintNames(String tableName)
	{

		String schema = jdbcTemplateDao.getJdbcTemplate().queryForObject("SELECT DATABASE()", String.class);

		String sql = "select * from INFORMATION_SCHEMA.TABLE_CONSTRAINTS where CONSTRAINT_TYPE = 'FOREIGN KEY' AND TABLE_SCHEMA = '" + schema
			+ "' AND TABLE_NAME='" + tableName + "'";

		List<String> existingConstraintNames = jdbcTemplateDao.getJdbcTemplate()
			.query(sql, new RowMapperResultSetExtractor<String>(new RowMapper<String>()
			{
				@Override
				public String mapRow(ResultSet rs, int arg1) throws SQLException
				{
					return rs.getString("CONSTRAINT_NAME");
				}
			}));
		return existingConstraintNames;
	}
}
