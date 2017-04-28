package com.holgerhees.shared.web.view;

import com.holgerhees.shared.web.model.Request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageView extends View
{

    private int BUFFER_LENGTH = 4096;
    private int code = HttpServletResponse.SC_OK;
    private File file;
    private InputStream input;
    private String mimetype;

    public ImageView( Request request, File file, InputStream input, String mimetype )
    {
        super( request );
        this.file = file;
        this.input = input;
        this.mimetype = mimetype;
    }

    @Override
    public void render() throws ServletException, IOException
    {
        getRequest().getHttpResponse().setStatus( this.code );
        getRequest().getHttpResponse().setContentLength( (int) file.length() );
        getRequest().getHttpResponse().setContentType( mimetype );

        OutputStream output = getRequest().getHttpResponse().getOutputStream();
        byte[] bytes = new byte[BUFFER_LENGTH];
        int read = 0;
        while( ( read = input.read( bytes, 0, BUFFER_LENGTH ) ) != -1 )
        {
            output.write( bytes, 0, read );
            output.flush();
        }

        input.close();
        output.close();
    }
}
