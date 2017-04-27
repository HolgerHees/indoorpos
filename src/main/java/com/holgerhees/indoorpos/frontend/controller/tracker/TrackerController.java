package com.holgerhees.indoorpos.frontend.controller.tracker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.indoorpos.persistance.dao.BeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackedBeaconDAO;
import com.holgerhees.indoorpos.persistance.dto.BeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;
import com.holgerhees.web.model.Request;
import com.holgerhees.web.util.GSonFactory;
import com.holgerhees.web.view.TextView;
import com.holgerhees.web.view.View;

@Component("trackerController")
public class TrackerController implements Controller
{
	private class TrackedBeacon
	{
		private String uuid;
		private int power;
	}

	private class Parameter
	{
		private Long trackerId;
		private List<TrackedBeacon> trachedBeacon;
	}

	@Autowired
	private BeaconDAO beaconDAO;

	@Autowired
	private TrackedBeaconDAO trackedBeaconDAO;

	@Override
	final public View handle(Request req)
	{
		byte[] body = null;

		try
		{
			StringBuilder jb = new StringBuilder();
			InputStream reader = req.getHttpRequest().getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte buf[] = new byte[1024];
			int count;
			while ((count = reader.read(buf)) > 0)
			{
				baos.write(buf, 0, count);
			}
			body = baos.toByteArray();
		}
		catch (IOException e)
		{

		}

		if (body == null || body.length == 0)
		{
			return new TextView(req, "empty request");
		}

		String json = new String(body, Charset.defaultCharset());

		Parameter param = GSonFactory.createGSon().fromJson(json, Parameter.class);

		Map<String, BeaconDTO> beaconDTOMap = beaconDAO.getBeaconUUIDMap();

		for (TrackedBeacon beacon : param.trachedBeacon)
		{
			BeaconDTO beaconDTO = beaconDTOMap.get(beacon.uuid);

			TrackedBeaconDTO trackedBeaconDTO = new TrackedBeaconDTO();
			trackedBeaconDTO.setTrackerId(param.trackerId);
			trackedBeaconDTO.setBeaconId(beaconDTO.getId());
			trackedBeaconDTO.setPower(beacon.power);

			beaconDAO.save(beaconDTO);
		}

		return new TextView(req, "tracked");
	}
}
