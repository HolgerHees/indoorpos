package com.holgerhees.shared.web.view;

import com.holgerhees.shared.web.model.Request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TextView extends View
{

    private int code = HttpServletResponse.SC_OK;
    private String text;

    public TextView( Request request, String text, int code )
    {
        super( request );
        this.text = text;
        this.code = code;
    }

    public TextView( Request request, String text )
    {
        super( request );
        this.text = text;
    }

    @Override
    public void render() throws ServletException, IOException
    {
        getRequest().getHttpResponse().setStatus( this.code );
        getRequest().getHttpResponse().setContentType( "text/plain" );
        getRequest().getHttpResponse().setCharacterEncoding( "utf-8" );
        getRequest().getHttpResponse().getWriter().print( text );
    }

    public String getText()
    {
        return text;
    }
}
