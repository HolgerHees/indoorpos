package com.holgerhees.indoorpos.frontend.controller;

import com.holgerhees.web.model.Request;
import com.holgerhees.web.view.View;

public interface Controller {

	public View handle(Request request);
}
