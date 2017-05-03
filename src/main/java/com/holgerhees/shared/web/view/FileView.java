package com.holgerhees.shared.web.view;

import com.holgerhees.shared.web.model.Request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;

public class FileView extends View
{

    private HttpServlet defaultServlet;

    public FileView( HttpServlet defaultServlet, Request request )
    {
        super( request );
        this.defaultServlet = defaultServlet;
    }

    @Override
    public void render() throws ServletException, IOException
    {
        defaultServlet.service( getRequest().getHttpRequest(), getRequest().getHttpResponse() );
    }
}
