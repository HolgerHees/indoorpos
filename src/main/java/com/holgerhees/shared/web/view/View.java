package com.holgerhees.shared.web.view;

import com.holgerhees.shared.web.model.Request;

import javax.servlet.ServletException;
import java.io.IOException;

public abstract class View
{

    private Request request;

    public View(Request request)
    {
        this.request = request;
    }

    public abstract void render() throws ServletException, IOException;

    public Request getRequest()
    {
        return request;
    }
}
