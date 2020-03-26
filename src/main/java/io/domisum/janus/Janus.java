package io.domisum.janus;

import com.google.inject.Inject;
import io.domisum.janus.intercom.JanusIntercomServer;
import io.domisum.lib.auxiliumlib.util.java.thread.ThreadUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Janus
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// CONSTANTS
	private static final Duration EMERGENCY_EXIT_DELAY = Duration.ofMinutes(5);
	
	// DEPENDENCIES
	private final JanusIntercomServer intercomServer;
	
	
	// START
	public void start()
	{
		logger.info("Starting...");
		
		loadConfiguration();
		
		intercomServer.start();
		// TODO ticker
		
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
		
		intercomServer.stop();
		
		logger.info("Shutdown sequence complete, exiting...");
	}
	
}
