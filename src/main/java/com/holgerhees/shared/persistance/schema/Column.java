package com.holgerhees.shared.persistance.schema;

import java.lang.reflect.Method;

public class Column
{
    private String name;
    private Class<?> type;
    private Method setter;
    private Method setterConverter;
    private Method getter;
    private Method getterConverter;
    private boolean insertable;
    private boolean updateable;
    private String definition;

    private Table table;
    private Constraint constraint;

    public Column( String name, boolean insertable, boolean updateable, Table table )
    {
        this.name = name;
        this.insertable = insertable;
        this.updateable = updateable;
        this.table = table;
    }

    public Method getSetter()
    {
        return setter;
    }

    public void setSetter( Method setter )
    {
        this.setter = setter;
    }

    public Method getSetterConverter()
    {
        return setterConverter;
    }

    public void setSetterConverter( Method setterConverter )
    {
        this.setterConverter = setterConverter;
    }

    public Method getGetter()
    {
        return getter;
    }

    public void setGetter( Method getter )
    {
        this.getter = getter;
    }

    public Method getGetterConverter()
    {
        return getterConverter;
    }

    public void setGetterConverter( Method getterConverter )
    {
        this.getterConverter = getterConverter;
    }

    public boolean isInsertable()
    {
        return insertable;
    }

    public boolean isUpdateable()
    {
        return updateable;
    }

    public String getDefinition()
    {
        return definition;
    }

    public void setDefinition( String definition )
    {
        this.definition = definition;
    }

    public Table getTable()
    {
        return table;
    }

    public Constraint getConstraint()
    {
        return constraint;
    }

    public void setConstraint( Constraint constraint )
    {
        this.constraint = constraint;
    }

    public String getName()
    {
        return name;
    }

    public Class<?> getType()
    {
        return type;
    }

    public void setType( Class<?> type )
    {
        this.type = type;
    }
}
