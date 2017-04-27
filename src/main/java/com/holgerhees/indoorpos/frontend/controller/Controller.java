package com.holgerhees.indoorpos.frontend.controller;

import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.view.View;

public interface Controller
{

	public View handle(Request request);
}
