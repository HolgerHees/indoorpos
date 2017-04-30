package com.holgerhees.indoorpos.frontend.service;

import com.google.gson.JsonElement;
import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.indoorpos.persistance.dao.AreaDAO;
import com.holgerhees.indoorpos.persistance.dao.RoomDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackedBeaconDAO;
import com.holgerhees.indoorpos.persistance.dao.TrackerDAO;
import com.holgerhees.indoorpos.persistance.dto.AreaDTO;
import com.holgerhees.indoorpos.persistance.dto.RoomDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackedBeaconDTO;
import com.holgerhees.indoorpos.persistance.dto.TrackerDTO;
import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.util.GSonFactory;
import com.holgerhees.shared.web.view.GsonView;
import com.holgerhees.shared.web.view.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component( "cacheService" )
public class CacheService
{
    public static int MIN_SAMPLES = 3;
    public static int MIN_RSSI = -88;

    public static int MIN_INTERVAL = 2000;
    private static int MAX_INTERVAL = 10000;

    private int interval = 0;
    private long lastTrackerUpdate = 0;
    private long lastTrackerId = 0;

    public void trackerUdate( TrackerDTO trackerDTO )
    {
        long currentTrackerUpdate = System.currentTimeMillis();

        if( lastTrackerUpdate > 0 && lastTrackerId == trackerDTO.getId() )
        {
            interval = (int) ( currentTrackerUpdate - lastTrackerUpdate );

            if( interval > MAX_INTERVAL )
            {
                interval = MAX_INTERVAL;
            }
        }
        else
        {
            interval = 0;
        }

        lastTrackerUpdate = currentTrackerUpdate;
        lastTrackerId = trackerDTO.getId();
    }

    public int getTrackerInterval()
    {
        return interval;
    }

    public long getLastTrackerUpdate()
    {
        return lastTrackerUpdate;
    }
}
