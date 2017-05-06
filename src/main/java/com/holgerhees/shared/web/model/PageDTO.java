package com.holgerhees.shared.web.model;

public class PageDTO
{

    private String cssPrefix;
    private String jsPrefix;
    private String imgPrefix;
    private String server;

    public String getCssPrefix()
    {
        return cssPrefix;
    }

    public void setCssPrefix( String cssPrefix )
    {
        this.cssPrefix = cssPrefix;
    }

    public String getJsPrefix()
    {
        return jsPrefix;
    }

    public void setJsPrefix( String jsPrefix )
    {
        this.jsPrefix = jsPrefix;
    }

    public String getImgPrefix()
    {
        return imgPrefix;
    }

    public void setImgPrefix( String imgPrefix )
    {
        this.imgPrefix = imgPrefix;
    }

    public String getServer()
    {
        return server;
    }

    public void setServer( String server )
    {
        this.server = server;
    }
}
