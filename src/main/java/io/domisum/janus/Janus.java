package io.domisum.janus;

import com.google.inject.Inject;
import io.domisum.janus.build.LatestBuildRegistry;
import io.domisum.janus.config.Configuration;
import io.domisum.janus.config.ConfigurationLoader;
import io.domisum.janus.config.object.project.Project;
import io.domisum.janus.intercom.IntercomServer;
import io.domisum.lib.auxiliumlib.contracts.ApplicationStopper;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import io.domisum.lib.auxiliumlib.util.java.thread.ThreadUtil;
import io.domisum.lib.auxiliumlib.util.java.thread.ThreadWatchdog;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Janus
		implements ApplicationStopper
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// CONSTANTS
	public static final File CONFIG_DIRECTORY = new File("config");
	private static final Duration EMERGENCY_EXIT_DELAY = Duration.ofMinutes(10);
	
	// DEPENDENCIES
	private final ConfigurationLoader configurationLoader;
	private final LatestBuildRegistry latestBuildRegistry;
	private final IntercomServer intercomServer;
	private final JanusTicker janusTicker;
	
	// CONFIGURATION
	private Configuration configuration;
	
	
	// START
	public void start()
	{
		logger.info("Starting...");
		
		boolean configurationValid = loadConfiguration();
		if(!configurationValid)
			return;
		readLatestBuilds();
		
		ThreadWatchdog.registerOnTerminationAction(Thread.currentThread(), this::stop);
		intercomServer.start();
		janusTicker.start();
		
		ThreadWatchdog.unregisterOnTerminationActions(Thread.currentThread());
		logger.info("Startup complete\n");
	}
	
	private boolean loadConfiguration()
	{
		try
		{
			configuration = configurationLoader.load();
			janusTicker.setConfiguration(configuration);
			return true;
		}
		catch(InvalidConfigurationException e)
		{
			logger.error("Invalid configuration, shutting down", e);
			stop();
			return false;
		}
	}
	
	private void readLatestBuilds()
	{
		var projects = configuration.getProjectRegistry().getAll();
		for(var project : projects)
		{
			var buildRootDirectory = project.getBuildRootDirectory();
			if(buildRootDirectory == null)
				continue;
			
			var latestBuildFile = new File(buildRootDirectory, Project.LATEST_BUILD_FILE_NAME);
			if(!latestBuildFile.exists())
				continue;
			
			String latestBuildName = FileUtil.readString(latestBuildFile);
			latestBuildRegistry.set(project.getId(), latestBuildName);
		}
	}
	
	
	// STOP
	@Override
	public void stop()
	{
		ThreadUtil.scheduleEmergencyExit(EMERGENCY_EXIT_DELAY);
		logger.info("Initiating shutdown sequence...");
		
		janusTicker.stop();
		intercomServer.stop();
		
		logger.info("...Shutdown sequence complete");
	}
	
}
