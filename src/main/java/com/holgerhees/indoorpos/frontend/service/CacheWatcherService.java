package com.holgerhees.indoorpos.frontend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by hhees on 04.05.17.
 */
@Component( "cacheWatcherService" )
public class CacheWatcherService
{
	@Autowired
	CacheService cacheService;

	private Thread watcher;

	private long lastUpdate;
	private long lastPush;

	private static List<CacheWatcherClient> watcherClients = Collections.synchronizedList( new ArrayList<>());

	public class Watcher implements Runnable
	{
		public Watcher()
		{
		}

		@Override
		public void run()
		{
			while( !Thread.currentThread().isInterrupted() )
			{
				try
				{
					//LOGGER.info( "Area watcher" );
					Thread.sleep( 100 );

					if( lastUpdate != cacheService.getLastUpdate() && System.currentTimeMillis() - lastPush > 500 )
					{
						lastUpdate = cacheService.getLastUpdate();
						lastPush = System.currentTimeMillis();

						for( CacheWatcherClient client: watcherClients )
						{
							client.notifyCacheChange();
						}
					}
				}
				catch( InterruptedException e )
				{
					//e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	@PostConstruct
	public void init()
	{
		watcher = new Thread( new Watcher() );
		watcher.setDaemon( true );
		watcher.start();
	}

	public void shutdown()
	{
		watcher.interrupt();
	}

	public void addWatcher( CacheWatcherClient client )
	{
		watcherClients.add( client );
	}
}
