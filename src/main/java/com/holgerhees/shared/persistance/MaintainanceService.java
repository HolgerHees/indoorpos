package com.holgerhees.shared.persistance;

import com.holgerhees.shared.persistance.dao.helper.SchemaDAO;
import com.holgerhees.shared.persistance.schema.Column;
import com.holgerhees.shared.persistance.schema.Index;
import com.holgerhees.shared.persistance.schema.Table;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component( "maintainanceService" )
public class MaintainanceService
{
    private static Log LOGGER = LogFactory.getLog( MaintainanceService.class );

    @Autowired
    SchemaService schemaService;

    @Autowired
    SchemaDAO schemaDao;

    public void createDatabaseSchema( boolean dropTable )
    {
        List<String> existingTables = schemaDao.getExistingTables();

        if( dropTable )
        {

            LinkedList<Table> dropTableList = schemaService.getTablesInDropOrder();
            for( Table table : dropTableList )
            {

                if( !existingTables.contains( table.getName() ) )
                {
                    continue;
                }

                LOGGER.info( "DROP TABLE " + table.getName() );
                schemaDao.dropTable( table.getDtoClass() );
            }

            existingTables = schemaDao.getExistingTables();
        }

        LinkedList<Table> createTableList = schemaService.getTablesInCreationOrder();
        for( Table table : createTableList )
        {

            Map<String, String> existingColumns = existingTables.contains( table.getName() ) ?
                    schemaDao.getExistingColumns( table.getName() ) :
                    new HashMap<>();
            if( existingColumns.size() > 0 )
            {
                boolean skipped = true;

                // ADD missing Columns
                String previousColumn = null;
                for( Column column : table.getColumns() )
                {
                    if( !existingColumns.containsKey( column.getName() ) )
                    {
                        LOGGER.info( "ALTER TABLE " + table.getName() + " ADD COLUMN " + column.getName() );

                        schemaDao.createColumn( table.getName(), column.getDefinition(), previousColumn );
                        skipped = false;
                    } else if( !existingColumns.get( column.getName() ).equals( column.getDefinition() ) )
                    {
                        LOGGER.error(
                                "Changing of column definition of column '" + column.getName() + "' in table '" + table.getName() + "' not implemented" );
                        skipped = false;
                    }

                    previousColumn = column.getName();
                    existingColumns.remove( column.getName() );
                }

                // REMOVE unused Columns
                if( existingColumns.size() > 0 )
                {
                    LOGGER.error( "Removing of unsed column '" + StringUtils.join( existingColumns.keySet(), ", " ) + "' in table '" + table.getName()
                                          + "' not implemented" );
                    skipped = false;
                }

                // ADD missing Indexes
                List<String> existingIndexes = schemaDao.getExistingIndexNames( table.getName() );
                for( Index index : table.getIndexes() )
                {
                    if( !existingIndexes.contains( index.getName() ) )
                    {
                        LOGGER.info( "ALTER TABLE " + table.getName() + " ADD INDEX " + index.getName() );
                        schemaDao.createIndex( table.getName(), index.getDefinition() );
                        skipped = false;
                    }

                    existingIndexes.remove( index.getName() );
                }

                // Remove unused Indexes
                if( !existingIndexes.isEmpty() )
                {
                    for( String indexName : existingIndexes )
                    {
                        LOGGER.info( "ALTER TABLE " + table.getName() + " DROP INDEX " + indexName );
                        schemaDao.dropIndex( table.getName(), indexName );
                        skipped = false;
                    }
                }

                // ADD missing Constraints
                List<String> existingConstraints = schemaDao.getExistingConstraintNames( table.getName() );
                for( Column column : table.getColumns() )
                {
                    if( column.getConstraint() == null )
                    {
                        continue;
                    }

                    if( !existingConstraints.contains( column.getConstraint().getName() ) )
                    {
                        LOGGER.info( "ALTER TABLE " + table.getName() + " ADD " + column.getConstraint().getDefinition() );
                        schemaDao.createConstraint( table.getName(), column.getConstraint().getDefinition() );
                        skipped = false;
                    }

                    existingConstraints.remove( column.getConstraint().getName() );
                }

                // Remove unused Constraints
                if( !existingConstraints.isEmpty() )
                {
                    for( String constraintName : existingConstraints )
                    {
                        LOGGER.info( "ALTER TABLE " + table.getName() + " DROP CONSTRAINT " + constraintName );
                        schemaDao.dropConstraint( table.getName(), constraintName );
                        skipped = false;
                    }
                }

                if( skipped )
                {
                    LOGGER.info( "SKIP unchanged TABLE '" + table.getName() + "'" );
                }
            } else
            {
                LOGGER.info( "CREATE TABLE " + table.getName() );
                schemaDao.createTable( table, true );
            }
        }
    }

    public void showDatabaseSchema()
    {
        System.out.println( "\n\n" );

        LinkedList<Table> createTableList = schemaService.getTablesInCreationOrder();
        for( Table table : createTableList )
        {

            System.out.println( schemaDao.getCreateTableStatement( table, true ) + "\n\n" );
        }
    }

}
