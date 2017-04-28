package com.holgerhees.indoorpos.frontend.controller.overview;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.web.PageDtoInitService;
import com.holgerhees.web.model.Request;
import com.holgerhees.web.util.GSonFactory;
import com.holgerhees.web.view.GsonView;
import com.holgerhees.web.view.JspView;
import com.holgerhees.web.view.View;

@Component( "overviewController" )
public class OverviewTrackerController implements Controller
{
	@Autowired
	TrackerDAO trackerDAO;

	private class Tracker
	{
		String name;
		int posX;
		int posY;
	}

	@Override
	public View handle(Request request)
	{
		List<TrackerDTO> trackers = trackerDAO.getTracker();

		List<Tracker> result = new ArrayList<>();
		for( TrackerDTO tracker: trackers )
		{
			Tracker _tracker = new Tracker();
			_tracker.name = tracker.getName();
			_tracker.posX = tracker.getPosX();
			_tracker.posY = tracker.getPosY();

			result.add(_tracker);
		}

		JsonElement json = GSonFactory.createGSon().toJsonTree(result);

		return new GsonView(json, request);
	}

}
