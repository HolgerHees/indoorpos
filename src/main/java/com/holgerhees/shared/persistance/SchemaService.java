package com.holgerhees.shared.persistance;

import com.holgerhees.shared.persistance.annotations.DbColumn;
import com.holgerhees.shared.persistance.annotations.DbForeignKey;
import com.holgerhees.shared.persistance.annotations.DbIndex;
import com.holgerhees.shared.persistance.annotations.DbTable;
import com.holgerhees.shared.persistance.dto.AbstractBaseDTO;
import com.holgerhees.shared.persistance.model.Persistance;
import com.holgerhees.shared.persistance.schema.Column;
import com.holgerhees.shared.persistance.schema.Constraint;
import com.holgerhees.shared.persistance.schema.Index;
import com.holgerhees.shared.persistance.schema.Table;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;

@Component( "schemaService" )
public class SchemaService
{
    private Map<String, Table> tables = new HashMap<>();

    private String prefix;

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public void init()
    {

        Reflections reflections = new Reflections(prefix);
        Set<Class<? extends AbstractBaseDTO>> classes = reflections.getSubTypesOf(AbstractBaseDTO.class);

        Map<Column, DbForeignKey> tmpConstraintMapping = new HashMap<>();

        for( Class<?> mappedClass : classes )
        {

            if( Modifier.isAbstract(mappedClass.getModifiers()) )
            {
                continue;
            }

            Table table = buildTable(mappedClass, tmpConstraintMapping);

            if( tables.containsKey(table.getName()) )
            {
                throw new RuntimeException("Duplicate table \"" + table.getName() + "\"");
            }

            tables.put(table.getName(), table);
        }

        for( Entry<Column, DbForeignKey> entry : tmpConstraintMapping.entrySet() )
        {

            Class<?> targetTableClass = entry.getValue().target();
            DbTable targetDbTable = targetTableClass.getAnnotation(DbTable.class);

            Table targetTable = tables.get(targetDbTable.name());
            Column targetColumn = targetTable.getColumn(entry.getValue().field());

            entry.getKey().getConstraint().setTarget(targetColumn);
        }
    }

    public Table getTable(Class<?> mappedClass)
    {
        DbTable table = mappedClass.getAnnotation(DbTable.class);
        return tables.get(table.name());
    }

    private Table buildTable(Class<?> mappedClass, Map<Column, DbForeignKey> tmpConstraintMapping)
    {
        try
        {
            DbTable dbTable = mappedClass.getAnnotation(DbTable.class);

            List<Column> uniqueColumns = new ArrayList<>();
            List<Column> primaryColumns = new ArrayList<>();

            List<Field> fields = SchemaService.getAllFields(new LinkedList<Field>(), mappedClass);

            // Columns definitions
            Map<String, List<String>> indexColumns = new HashMap<>();
            Map<String, DbIndex.Type> indexTypes = new HashMap<>();

            Table table = new Table(mappedClass, dbTable.name());

            for( int i = 0; i < fields.size(); i++ )
            {
                Field field = fields.get(i);

                DbColumn dbColumn = field.getAnnotation(DbColumn.class);
                if( dbColumn == null )
                {
                    continue;
                }

                DbIndex index = field.getAnnotation(DbIndex.class);

                Column column = new Column(dbColumn.name(), dbColumn.insertable(), dbColumn.updatable(), table);

                String fieldName = field.getName();
                String baseName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

                String getterName = field.getType().equals(boolean.class) ? "is" + baseName : "get" + baseName;
                String setterName = "set" + baseName;

                column.setSetter(mappedClass.getMethod(setterName, field.getType()));
                column.setGetter(mappedClass.getMethod(getterName));

                if( Persistance.class.isAssignableFrom(field.getType()) )
                {
                    column.setSetterConverter(field.getType().getMethod("fromPersistenceID", Integer.class));
                    column.setGetterConverter(field.getType().getMethod("getPersistenceID"));
                    column.setType(Integer.class);
                } else
                {
                    column.setType(field.getType());
                }

                table.addColumn(column);

                buildColumnDefinition(column, dbColumn,
                        (!field.getType().equals(String.class) && index != null && index.type() == DbIndex.Type.PRIMARY_KEY && index.group().isEmpty()));

                if( dbColumn.foreignKey().length > 0 )
                {
                    buildConstraintDefinition(column, dbTable, dbColumn, tmpConstraintMapping);

                    if( index == null || (!index.group().isEmpty() && indexColumns.containsKey(getGroupId(index, i))) )
                    {
                        addIndex(DbIndex.DEFAULT, dbColumn, indexColumns, indexTypes, i);
                    }
                }

                if( index != null )
                {

                    if( index.type() == DbIndex.Type.PRIMARY_KEY )
                    {
                        primaryColumns.add(column);
                    } else if( index.type() == DbIndex.Type.UNIQUE )
                    {
                        uniqueColumns.add(column);
                    }

                    addIndex(index, dbColumn, indexColumns, indexTypes, i);
                }
            }

            // Columns indexes
            for( String groupId : indexColumns.keySet() )
            {
                Index index = buildIndexDefinition(groupId, indexColumns, indexTypes);
                table.addIndex(index);
            }

            if( !primaryColumns.isEmpty() )
            {
                table.setPrimaryColumns(primaryColumns.toArray(new Column[primaryColumns.size()]));
            } else if( !uniqueColumns.isEmpty() )
            {
                table.setPrimaryColumns(uniqueColumns.toArray(new Column[uniqueColumns.size()]));
            }

			/*System.out.println(table.getName());
            for( Column column: table.getPrimaryColumns())
			{
				System.out.println("     "+column.getName());
			}*/

            return table;
        } catch( NoSuchMethodException | SecurityException e )
        {
            throw new PersistanceException("Unable to build TableInfo for class " + mappedClass.getName(), e);
        }
    }

    private String getGroupId(DbIndex index, int i)
    {
        String groupId;
        if( index.type() == DbIndex.Type.PRIMARY_KEY )
        {
            groupId = DbIndex.Type.PRIMARY_KEY.name();
        } else if( !index.group().isEmpty() )
        {
            groupId = index.group();
        } else
        {
            groupId = "group_" + i;
        }
        return groupId;
    }

    private void addIndex(DbIndex index, DbColumn dbColumn, Map<String, List<String>> indexColumns, Map<String, DbIndex.Type> indexTypes, int i)
    {

        String groupId = getGroupId(index, i);

        if( !indexColumns.containsKey(groupId) )
        {
            indexColumns.put(groupId, new LinkedList<String>());
            indexTypes.put(groupId, index.type());
        }

        indexColumns.get(groupId).add(dbColumn.name());
    }

    private void buildColumnDefinition(Column column, DbColumn dbColumn, boolean isAutoincrement)
    {

        String definition = "`" + dbColumn.name() + "` " + dbColumn.type() + " " + (dbColumn.nullable() ? "NULL" : "NOT NULL");
        if( isAutoincrement )
        {
            definition += " AUTO_INCREMENT";
        }

        column.setDefinition(definition);
    }

    private void buildConstraintDefinition(Column column, DbTable dbTable, DbColumn dbColumn, Map<Column, DbForeignKey> tmpConstraintMapping)
    {

        StringBuilder fsb = new StringBuilder();

        String columnName = dbColumn.name();
        String constraintName = dbTable.name() + "_" + columnName + "_fk";

        fsb.append("CONSTRAINT `" + constraintName + "` FOREIGN KEY (`" + columnName + "`) REFERENCES ");

        DbForeignKey foreignKey = dbColumn.foreignKey()[0];

        tmpConstraintMapping.put(column, foreignKey);

        DbTable foreignTable = getTableAnnotation(foreignKey.target());
        Field foreignField = getField(foreignKey);

        DbColumn foreignColumn = foreignField.getAnnotation(DbColumn.class);
        if( foreignColumn == null )
        {
            throw new PersistanceException(
                    "@Column annotation missing on field [" + foreignKey.field() + "] in class " + foreignKey.target().getName());
        }

        fsb.append(foreignTable.name() + " (" + foreignColumn.name() + ") ");

        fsb.append("ON DELETE " + foreignKey.onDelete() + " ON UPDATE " + foreignKey.onUpdate());

        Constraint constraint = new Constraint(constraintName, fsb.toString());

        column.setConstraint(constraint);
    }

    private Index buildIndexDefinition(String groupId, Map<String, List<String>> indexColumns, Map<String, DbIndex.Type> indexTypes)
    {

        DbIndex.Type type = indexTypes.get(groupId);
        String[] columns = indexColumns.get(groupId).toArray(new String[]{});

        Index index = new Index(DbIndex.Type.getName(type, columns), type.getSqlStatement(columns));
        return index;
    }

    public LinkedList<Table> getTablesInDropOrder()
    {
        LinkedList<Table> list = getTablesInCreationOrder();
        Collections.reverse(list);
        return list;
    }

    public LinkedList<Table> getTablesInCreationOrder()
    {

        List<Table> process = new ArrayList<>(tables.values());

        LinkedList<Table> creationOrder = new LinkedList<>();
        while( !process.isEmpty() )
        {
            for( Table table : process )
            {
                boolean found = true;
                for( Column column : table.getColumns() )
                {

                    if( column.getConstraint() != null && !creationOrder.contains(column.getConstraint().getTarget().getTable()) )
                    {
                        found = false;
                        break;
                    }
                }

                if( found )
                {
                    creationOrder.add(table);
                    process.remove(table);
                    break;
                }
            }
        }

        for( Table def : creationOrder )
        {

            //System.out.println(def.getName());
            for( Column column : def.getColumns() )
            {
                //System.out.println("\t"+column.getDefinition());
            }
            for( Column column : def.getColumns() )
            {
                if( column.getConstraint() != null )
                {
                    //System.out.println("\t"+column.getConstraint().getDefinition() );
                }
            }
            for( Index index : def.getIndexes() )
            {
                //System.out.println("\t"+index.getDefinition());
            }
        }

        //throw new RuntimeException("test");

        return creationOrder;
    }

    public static List<Field> getAllFields(List<Field> fields, Class<?> type)
    {

        if( type.getSuperclass() != null )
        {
            fields = getAllFields(fields, type.getSuperclass());
        }

        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        return fields;
    }

    protected static Field getField(DbForeignKey foreignKey)
    {

        List<Field> fields = getAllFields(new LinkedList<Field>(), foreignKey.target());
        for( Field field : fields )
        {
            if( field.getName().equals(foreignKey.field()) )
            {
                return field;
            }
        }

        throw new PersistanceException(
                "Field [" + foreignKey.field() + "] for foreign key in class [" + foreignKey.target().getName() + "] doesn't exist.");
    }

    public static DbTable getTableAnnotation(Class<?> mappedClass)
    {
        DbTable table = mappedClass.getAnnotation(DbTable.class);

        if( table == null )
        {
            throw new PersistanceException("the provided class " + mappedClass.getName() + " is not annotated with @Table");
        }

        return table;
    }
}
