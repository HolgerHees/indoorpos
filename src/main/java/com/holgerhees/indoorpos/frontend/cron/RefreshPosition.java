package com.holgerhees.indoorpos.frontend.cron;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RefreshPosition
{
	private static Log LOGGER = LogFactory.getLog(RefreshPosition.class);
	
	@Scheduled(cron="*/2 * * * * *") // every second
    public void run()
	{
		LOGGER.info("refresh position");
    }
}
