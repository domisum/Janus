package io.domisum.janus;

import com.google.inject.Inject;
import io.domisum.janus.component.JanusComponent;
import io.domisum.janus.component.JanusComponentLoader;
import io.domisum.janus.intercom.JanusIntercomServer;
import io.domisum.lib.auxiliumlib.util.java.thread.ThreadUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.Set;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Janus
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// CONSTANTS
	public static final File CONFIG_DIRECTORY = new File("config");
	private static final Duration EMERGENCY_EXIT_DELAY = Duration.ofMinutes(5);
	
	// DEPENDENCIES
	private final JanusComponentLoader janusComponentLoader;
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
		Set<JanusComponent> components = janusComponentLoader.load();
		System.out.println(components);
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
