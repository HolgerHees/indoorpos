package com.holgerhees.shared.persistance.schema;

public class Constraint
{
    private String name;
    private String definition;
    private Column target;

    public Constraint(String name, String definition)
    {
        this.name = name;
        this.definition = definition;
    }

    public String getName()
    {
        return name;
    }

    public String getDefinition()
    {
        return definition;
    }

    public void setTarget(Column target)
    {
        this.target = target;
    }

    public Column getTarget()
    {
        return target;
    }
}
