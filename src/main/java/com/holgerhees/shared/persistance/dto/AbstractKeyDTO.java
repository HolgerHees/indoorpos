package com.holgerhees.shared.persistance.dto;

import com.holgerhees.shared.persistance.annotations.DbColumn;
import com.holgerhees.shared.persistance.annotations.DbIndex;

public abstract class AbstractKeyDTO extends AbstractBaseDTO
{

    @DbColumn( name = "id",
            type = "int(11)",
            insertable = false,
            updatable = false )
    @DbIndex( type = DbIndex.Type.PRIMARY_KEY )
    private Long id;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }
}
