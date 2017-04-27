package com.holgerhees.indoorpos.frontend.controller;

import com.holgerhees.indoorpos.frontend.web.model.Request;
import com.holgerhees.indoorpos.frontend.web.view.View;

public interface Controller {

	public View handle(Request request);
}
