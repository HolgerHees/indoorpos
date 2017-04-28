package com.holgerhees.indoorpos.frontend.controller.files;

import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.view.FileView;
import com.holgerhees.shared.web.view.View;
import org.apache.catalina.servlets.DefaultServlet;
import org.springframework.stereotype.Component;

@Component( "fileController" )
public class FileController implements Controller
{

    @Override
    public View handle(Request req)
    {
        return new FileView((DefaultServlet) req.getValue("staticContentServlet"), req);
    }
}
