package com.holgerhees.shared.web.util;

import com.holgerhees.shared.web.model.Request;

import java.util.HashMap;
import java.util.Map;

public class RequestUtils
{
    private RequestUtils()
    {
    }

    public static String getProtocolFromUrl( String url )
    {
        return url == null || url.indexOf( "://" ) == -1 ? null : url.substring( 0, url.indexOf( "://" ) );
    }

    public static Map<String, String> getParameterMap( Request req )
    {
        Map<String, String> map = new HashMap<>();

        if( req.getHttpRequest().getParameterMap() == null || req.getHttpRequest().getParameterMap().isEmpty() )
        {
            return map;
        }

        for( Object key : req.getHttpRequest().getParameterMap().keySet() )
        {
            map.put( (String) key, req.getHttpRequest().getParameter( (String) key ) );
        }
        return map;
    }
}
