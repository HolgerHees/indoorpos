package com.holgerhees.shared.web.view;

import com.holgerhees.shared.web.model.Request;
import org.apache.catalina.servlets.DefaultServlet;

import javax.servlet.ServletException;
import java.io.IOException;

public class FileView extends View
{

    private DefaultServlet defaultServlet;

    public FileView( DefaultServlet defaultServlet, Request request )
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
