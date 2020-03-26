package io.domisum.janus;

import io.domisum.lib.auxiliumlib.util.java.thread.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class Janus
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// CONSTANTS
	private static final Duration EMERGENCY_EXIT_DELAY = Duration.ofMinutes(5);
	
	// DEPENDENCIES
	// TODO
	
	
	// START
	public void start()
	{
		logger.info("Starting...");
		
		loadConfiguration();
		
		// TODO intercom server
		// ticker
		
		logger.info("Startup complete\n");
	}
	
	private void loadConfiguration()
	{
	
	}
	
	
	// STOP
	public void stop()
	{
		ThreadUtil.scheduleEmergencyExit(EMERGENCY_EXIT_DELAY);
		logger.info("Initiating shutdown sequence...");
		
		// TODO
		
		logger.info("Shutdown sequence complete, exiting...");
	}
	
}
