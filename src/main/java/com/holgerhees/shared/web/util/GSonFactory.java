package com.holgerhees.shared.web.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GSonFactory
{
    private static Gson gson = null;

    static
    {
        GsonBuilder builder = new GsonBuilder();
        //gson = builder.setPrettyPrinting().serializeNulls().create();
        gson = builder.serializeNulls().create();
    }

    private GSonFactory()
    {
    }

    public static final Gson createGSon()
    {
        return gson;
    }
}
