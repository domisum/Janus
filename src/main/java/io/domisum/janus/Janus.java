package io.domisum.janus;

import com.google.inject.Inject;
import io.domisum.janus.config.object.component.ComponentLoader;
import io.domisum.janus.config.object.credentials.CredentialLoader;
import io.domisum.janus.config.object.project.ProjectLoader;
import io.domisum.janus.intercom.IntercomServer;
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
	private final CredentialLoader janusCredentialLoader;
	private final ComponentLoader janusComponentLoader;
	private final ProjectLoader janusProjectLoader;
	
	private final IntercomServer intercomServer;
	
	
	// START
	public void start()
	{
		ThreadWatchdog.registerOnTerminationAction(Thread.currentThread(), this::stop);
		logger.info("Starting...");
		
		loadConfiguration();
		
		intercomServer.start();
		// TODO ticker
		
		ThreadWatchdog.unregisterOnTerminationActions(Thread.currentThread());
		logger.info("Startup complete\n");
	}
	
	private void loadConfiguration()
	{
		var credentials = janusCredentialLoader.load();
		var components = janusComponentLoader.load();
		var projects = janusProjectLoader.load();
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
