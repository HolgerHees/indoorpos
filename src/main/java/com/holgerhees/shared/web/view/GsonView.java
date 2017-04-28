package com.holgerhees.shared.web.view;

import com.google.gson.JsonElement;
import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.util.GSonFactory;

import javax.servlet.ServletException;
import java.io.IOException;

public class GsonView extends View
{

    private String gson;

    public GsonView(JsonElement jsonElement, Request req)
    {
        super(req);
        this.gson = GSonFactory.createGSon().toJson(jsonElement);
    }

    @Override
    public void render() throws ServletException, IOException
    {
        getRequest().getHttpResponse().setContentType("application/json");
        getRequest().getHttpResponse().setCharacterEncoding("utf-8");

        getRequest().getHttpResponse().getWriter().print(gson);
    }

    public String toJSON()
    {
        return this.gson;
    }
}
