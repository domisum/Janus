package io.domisum.janus;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.domisum.janus.build.ProjectBuilder;
import io.domisum.janus.build.ProjectOldBuildsCleaner;
import io.domisum.janus.config.Configuration;
import io.domisum.janus.config.object.component.Component;
import io.domisum.janus.config.object.project.Project;
import io.domisum.lib.auxiliumlib.config.ConfigException;
import io.domisum.lib.auxiliumlib.contracts.ApplicationStopper;
import io.domisum.lib.auxiliumlib.thread.ticker.Ticker;
import io.domisum.lib.auxiliumlib.util.ExceptionUtil;
import io.domisum.lib.auxiliumlib.util.LoggerUtil;
import io.domisum.lib.auxiliumlib.util.StringListUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class JanusTicker
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// CONSTANTS
	private static final Duration TIMEOUT = Duration.ofMinutes(20);
	
	// DEPENDENCIES
	private final ProjectOldBuildsCleaner projectOldBuildsCleaner;
	private final ProjectBuilder projectBuilder;
	
	private final RuntimeCommandExecutor runtimeCommandExecutor;
	private final ApplicationStopper applicationStopper;
	
	// REFERENCES
	private final Ticker ticker = Ticker.create("ticker", Duration.ofSeconds(5), TIMEOUT, this::tick);
	
	// CONFIG
	@Setter
	private transient Configuration configuration = null;
	
	
	// CONTROL
	public void start()
	{
		ticker.start();
	}
	
	public void stop()
	{
		ticker.stopSoft();
	}
	
	
	// TICK
	private void tick()
	{
		cleanOldBuilds();
		
		var updatedComponentIds = updateComponents();
		if(updatedComponentIds.size() > 0)
			logger.info("Detected update in components: [{}]", StringListUtil.list(updatedComponentIds));
		
		if(Janus.FULL_REBUILD_INDICATOR.exists())
		{
			Janus.FULL_REBUILD_INDICATOR.delete();
			
			var projects = configuration.getProjectRegistry().getAll();
			projects.removeIf(Project::isJanusConfig);
			projects.removeIf(Project::isJanusJar);
			runBuilds(projects);
			return;
		}
		
		runBuildsOfComponents(updatedComponentIds);
	}
	
	private void cleanOldBuilds()
	{
		var projects = configuration.getProjectRegistry().getAll();
		for(var project : projects)
			projectOldBuildsCleaner.cleanOldBuilds(project);
	}
	
	
	// UPDATE
	private Set<String> updateComponents()
	{
		var changedComponentIds = new HashSet<String>();
		for(var component : configuration.getComponentRegistry().getAll())
		{
			boolean changed = updateComponent(component);
			if(changed)
				changedComponentIds.add(component.getId());
		}
		
		return changedComponentIds;
	}
	
	private boolean updateComponent(Component component)
	{
		try
		{
			return component.update();
		}
		catch(IOException e)
		{
			var level = shouldUpdateExceptionBeDowngraded(e) ? Level.INFO : Level.WARN;
			LoggerUtil.log(logger, level, "Failed to update component '{}'", component.getId(), e);
			
			return false;
		}
	}
	
	
	// BUILD
	private void runBuildsOfComponents(Set<String> changedComponentIds)
	{
		var projectsToBuild = getProjectsToBuild(changedComponentIds);
		runBuilds(projectsToBuild);
	}
	
	private void runBuilds(Collection<Project> projectsToBuild)
	{
		boolean restartAfterBuilds = false;
		var commandsToExecute = new HashSet<String>();
		for(var project : projectsToBuild)
			try
			{
				boolean shouldRestart = projectBuilder.build(project, configuration);
				if(shouldRestart)
					restartAfterBuilds = true;
				
				String command = project.getCommandToExecuteAfterBuild();
				if(command != null)
				{
					logger.info("Project '{}' scheduled to run a command after building successfully: {}", project.getId(), command);
					commandsToExecute.add(command);
				}
			}
			catch(ConfigException e)
			{
				logger.error("Build failed, shutting down", e);
				restartAfterBuilds = true;
				break;
			}
		
		for(String command : commandsToExecute)
			runtimeCommandExecutor.executeCommand(command);
		
		if(restartAfterBuilds)
		{
			logger.info("ProjectBuilder indicates restart, shutting down...");
			applicationStopper.stop();
		}
	}
	
	private Set<Project> getProjectsToBuild(Set<String> changedComponentIds)
	{
		var projectsToBuild = new HashSet<Project>();
		
		var projects = configuration.getProjectRegistry().getAll();
		for(var project : projects)
		{
			var changedProjectComponentIds = new ArrayList<String>();
			for(var projectComponent : project.getComponents())
				if(changedComponentIds.contains(projectComponent.getComponentId()))
					changedProjectComponentIds.add(projectComponent.getComponentId());
			
			if(changedProjectComponentIds.size() > 0)
			{
				logger.info("Scheduling build of project '{}'. Changed project components: [{}]",
					project.getId(), StringListUtil.list(changedProjectComponentIds));
				projectsToBuild.add(project);
			}
		}
		
		return projectsToBuild;
	}
	
	
	// UTIL
	private boolean shouldUpdateExceptionBeDowngraded(Exception updateException)
	{
		String exceptionSynopsisLowerCase = ExceptionUtil.getSynopsis(updateException).toLowerCase();
		
		if(StringUtils.containsAny(exceptionSynopsisLowerCase, "connection reset", "authentication not supported"))
			return true;
		if(StringUtils.containsAny(exceptionSynopsisLowerCase, "time out", "timed out", "timeout"))
			return true;
		if(StringUtils.containsAny(exceptionSynopsisLowerCase, "internal server error"))
			return true;
		
		return false;
	}
	
}
