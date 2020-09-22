package io.domisum.janus;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.domisum.janus.config.Configuration;
import io.domisum.janus.config.ConfigurationLoader;
import io.domisum.janus.config.object.component.Component;
import io.domisum.lib.auxiliumlib.config.ConfigException;
import io.domisum.lib.auxiliumlib.contracts.ApplicationStopper;
import io.domisum.lib.auxiliumlib.input.StopOnCliEnterPress;
import io.domisum.lib.auxiliumlib.thread.ThreadWatchdog;
import io.domisum.lib.auxiliumlib.util.ThreadUtil;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import io.domisum.lib.auxiliumlib.util.file.FileUtil.FileType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Janus
	implements ApplicationStopper
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// CONSTANTS
	public static final File CONFIG_DIRECTORY = new File("config");
	public static final File CONFIG_DIRECTORY_BACKUP = new File("config_backup");
	private static final File CONFIG_DIRECTORY_INVALID = new File("config_invalid");
	
	private static final File LOG_DIRECTORY = new File("logs");
	private static final int KEEP_LOG_FILES_NUMBER = 10;
	
	private static final Duration EMERGENCY_EXIT_DELAY = Duration.ofMinutes(10);
	
	// DEPENDENCIES
	private final ConfigurationLoader configurationLoader;
	private final JanusTicker janusTicker;
	
	// CONFIGURATION
	private Configuration configuration;
	
	
	// START
	public void start()
	{
		boolean configurationValid = loadConfiguration();
		if(!configurationValid)
			return;
		
		cleanUp();
		
		logger.info("Starting...");
		ThreadWatchdog.registerOnTerminationAction(Thread.currentThread(), this::stop);
		janusTicker.start();
		
		ThreadWatchdog.unregisterOnTerminationActions(Thread.currentThread());
		StopOnCliEnterPress.stopOnPress(this);
		logger.info("...Startup complete\n");
	}
	
	private boolean loadConfiguration()
	{
		try
		{
			configuration = configurationLoader.load(CONFIG_DIRECTORY);
			janusTicker.setConfiguration(configuration);
			
			FileUtil.deleteDirectory(CONFIG_DIRECTORY_INVALID);
			FileUtil.deleteDirectory(CONFIG_DIRECTORY_BACKUP);
			return true;
		}
		catch(ConfigException e)
		{
			logger.error("Invalid configuration, trying to restore backup", e);
			if(CONFIG_DIRECTORY_BACKUP.exists())
			{
				logger.info("Config directory backup exists, restoring...");
				FileUtil.deleteDirectory(CONFIG_DIRECTORY_INVALID);
				FileUtil.moveDirectory(CONFIG_DIRECTORY, CONFIG_DIRECTORY_INVALID);
				FileUtil.moveDirectory(CONFIG_DIRECTORY_BACKUP, CONFIG_DIRECTORY);
				logger.info("...Config directory backup restored");
			}
			
			logger.info("Restarting...");
			stop();
			return false;
		}
	}
	
	
	private void cleanUp()
	{
		deleteNoLongerUsedComponentDirs();
		deleteOldLogFiles();
	}
	
	private void deleteNoLongerUsedComponentDirs()
	{
		var components = configuration.getComponentRegistry().getAll();
		var currentComponentDirNames = components.stream()
			.map(Component::getDirectory)
			.map(File::getName)
			.collect(Collectors.toSet());
		
		var componentDirs = FileUtil.listFilesFlat(Component.COMPONENTS_DIRECTORY, FileType.DIRECTORY);
		for(var componentDir : componentDirs)
			if(!currentComponentDirNames.contains(componentDir.getName()))
			{
				logger.info("Deleting no longer used component directory: {}", componentDir);
				FileUtil.deleteDirectory(componentDir);
			}
	}
	
	private void deleteOldLogFiles()
	{
		var logFiles = FileUtil.listFilesRecursively(LOG_DIRECTORY, FileType.FILE);
		var fileExtensions = new HashSet<String>();
		for(var logFile : logFiles)
			fileExtensions.add(FileUtil.getCompositeExtension(logFile));
		
		int numberOfCombinedLogFilesToKeep = KEEP_LOG_FILES_NUMBER*fileExtensions.size();
		logFiles.stream()
			.sorted(Comparator.comparingLong(File::lastModified))
			.limit(logFiles.size()-numberOfCombinedLogFilesToKeep)
			.forEach(FileUtil::deleteFile);
	}
	
	
	// STOP
	@Override
	public void stop()
	{
		logger.info("Initiating shutdown sequence...");
		ThreadUtil.scheduleEmergencyExit(EMERGENCY_EXIT_DELAY);
		
		janusTicker.stop();
		
		logger.info("...Shutdown sequence complete");
	}
	
}
