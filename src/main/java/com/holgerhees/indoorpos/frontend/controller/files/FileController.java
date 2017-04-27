package com.holgerhees.indoorpos.frontend.controller.files;

import org.apache.catalina.servlets.DefaultServlet;
import org.springframework.stereotype.Component;

import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.indoorpos.frontend.web.model.Request;
import com.holgerhees.indoorpos.frontend.web.view.FileView;
import com.holgerhees.indoorpos.frontend.web.view.View;

@Component("fileController")
public class FileController implements Controller
{

	@Override
	public View handle(Request req)
	{
		return new FileView((DefaultServlet) req.getValue("staticContentServlet"), req);
	}
}
