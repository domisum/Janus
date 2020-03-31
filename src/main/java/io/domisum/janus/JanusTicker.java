package io.domisum.janus;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.domisum.janus.build.LatestBuildRegistry;
import io.domisum.janus.build.ProjectBuilder;
import io.domisum.janus.build.ProjectOldBuildsCleaner;
import io.domisum.janus.config.Configuration;
import io.domisum.janus.config.object.project.Project;
import io.domisum.lib.auxiliumlib.contracts.ApplicationStopper;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import io.domisum.lib.auxiliumlib.ticker.Ticker;
import io.domisum.lib.auxiliumlib.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class JanusTicker
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// DEPENDENCIES
	private final ProjectOldBuildsCleaner projectOldBuildsCleaner;
	private final ProjectBuilder projectBuilder;
	private final LatestBuildRegistry latestBuildRegistry;
	
	private final CommandExecutor commandExecutor;
	private final ApplicationStopper applicationStopper;
	
	// REFERENCES
	private final Ticker ticker = Ticker.create("ticker", Duration.ofSeconds(5), Duration.ofMinutes(10), this::tick);
	
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
	private void tick(Supplier<Boolean> shouldStop)
	{
		cleanOldBuilds();
		
		var updatedComponentIds = updateComponents();
		if(updatedComponentIds.size() > 0)
			logger.info("Detected update in components: [{}]", StringUtil.collectionToString(updatedComponentIds, ", "));
		
		runBuilds(updatedComponentIds);
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
		
		var components = configuration.getComponentRegistry().getAll();
		for(var component : components)
			try
			{
				boolean changed = component.update();
				if(changed)
					changedComponentIds.add(component.getId());
			}
			catch(IOException e)
			{
				logger.warn("Failed to update component '{}'", component.getId(), e);
			}
		
		return changedComponentIds;
	}
	
	
	// BUILD
	private void runBuilds(Set<String> changedComponentIds)
	{
		boolean restartAfterBuilds = false;
		var commandsToExecute = new HashSet<String>();
		
		var projectsToBuild = getProjectsToBuild(changedComponentIds);
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
			catch(InvalidConfigurationException e)
			{
				logger.error("Build failed, shutting down", e);
				restartAfterBuilds = true;
				break;
			}
		
		if(projectsToBuild.size() > 0)
			logger.info("Latest builds: {}\n", latestBuildRegistry.getReport());
		
		for(String command : commandsToExecute)
			commandExecutor.executeCommand(command);
		
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
						project.getId(), StringUtil.listToString(changedProjectComponentIds, ", "));
				projectsToBuild.add(project);
			}
		}
		
		return projectsToBuild;
	}
	
}
