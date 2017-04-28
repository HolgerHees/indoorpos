package com.holgerhees.shared.web.view;

import com.holgerhees.shared.web.model.Request;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;

public class JspView extends View
{
    private String jspFilename;

    public JspView(String jspFilename, Request request)
    {
        super(request);
        this.jspFilename = jspFilename;
    }

    @Override
    public void render() throws ServletException, IOException
    {
        RequestDispatcher dispatcher = getRequest().getServletContext().getRequestDispatcher(jspFilename);
        dispatcher.forward(getRequest().getHttpRequest(), getRequest().getHttpResponse());
    }

    public String getJspFilename()
    {
        return jspFilename;
    }
}