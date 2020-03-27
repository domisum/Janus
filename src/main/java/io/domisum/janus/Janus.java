package io.domisum.janus;

import com.google.inject.Inject;
import io.domisum.janus.config.Configuration;
import io.domisum.janus.config.ConfigurationLoader;
import io.domisum.janus.intercom.IntercomServer;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import io.domisum.lib.auxiliumlib.util.java.thread.ThreadUtil;
import io.domisum.lib.auxiliumlib.util.java.thread.ThreadWatchdog;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Janus
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// CONSTANTS
	public static final File CONFIG_DIRECTORY = new File("config");
	private static final Duration EMERGENCY_EXIT_DELAY = Duration.ofMinutes(5);
	
	// DEPENDENCIES
	private final ConfigurationLoader configurationLoader;
	private final IntercomServer intercomServer;
	
	// CONFIGURATION
	private Configuration configuration;
	
	
	// START
	public void start()
	{
		logger.info("Starting...");
		
		boolean configurationValid = loadConfiguration();
		if(!configurationValid)
			return;
		
		ThreadWatchdog.registerOnTerminationAction(Thread.currentThread(), this::stop);
		intercomServer.start();
		// TODO ticker
		
		ThreadWatchdog.unregisterOnTerminationActions(Thread.currentThread());
		logger.info("Startup complete\n");
	}
	
	private boolean loadConfiguration()
	{
		try
		{
			configuration = configurationLoader.load();
			return true;
		}
		catch(InvalidConfigurationException e)
		{
			logger.info("Invalid configuration, shutting down", e);
			stop();
			return false;
		}
	}
	
	
	// STOP
	public void stop()
	{
		ThreadUtil.scheduleEmergencyExit(EMERGENCY_EXIT_DELAY);
		logger.info("Initiating shutdown sequence...");
		
		intercomServer.stop();
		
		logger.info("Shutdown sequence complete, exiting...");
	}
	
}
