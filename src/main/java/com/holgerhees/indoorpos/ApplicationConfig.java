package com.holgerhees.indoorpos;

import org.springframework.stereotype.Component;

@Component( "applicationConfig" )
public class ApplicationConfig
{
    private Boolean production;

    /**
     * Called from DefaultConfigContext.xml
     */
    public void init()
    {
        // just a dummy placeholder
    }

    public Boolean isProduction()
    {
        return production;
    }

    public void setProduction( Boolean production )
    {
        this.production = production;
    }
}
