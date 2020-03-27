package io.domisum.janus;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.domisum.janus.build.LatestBuildRegistry;
import io.domisum.janus.build.ProjectBuilder;
import io.domisum.janus.build.ProjectOldBuildsCleaner;
import io.domisum.janus.config.Configuration;
import io.domisum.janus.config.object.project.Project;
import io.domisum.lib.auxiliumlib.ticker.Ticker;
import io.domisum.lib.auxiliumlib.util.StringUtil;
import lombok.Setter;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@Singleton
public class JanusTicker
		extends Ticker
{
	
	// DEPENDENCIES
	private final ProjectBuilder projectBuilder;
	private final LatestBuildRegistry latestBuildRegistry;
	private final ProjectOldBuildsCleaner projectOldBuildsCleaner;
	
	// CONFIG
	@Setter
	private transient Configuration configuration = null;
	
	
	// INIT
	@Inject
	public JanusTicker(ProjectBuilder projectBuilder, LatestBuildRegistry latestBuildRegistry, ProjectOldBuildsCleaner projectOldBuildsCleaner)
	{
		super("janusTicker", Duration.ofSeconds(5), Duration.ofMinutes(5));
		
		this.projectBuilder = projectBuilder;
		this.latestBuildRegistry = latestBuildRegistry;
		this.projectOldBuildsCleaner = projectOldBuildsCleaner;
	}
	
	
	// CONTROL
	public void stop()
	{
		stopSoft();
	}
	
	
	// TICK
	@Override
	protected void tick(Supplier<Boolean> shouldStop)
	{
		var updatedComponentIds = updateComponents();
		runBuilds(updatedComponentIds, shouldStop);
		
		if(!shouldStop.get())
			cleanOldBuilds();
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
				return new HashSet<>(); // don't trigger builds after failed update
			}
		
		return changedComponentIds;
	}
	
	
	// BUILD
	private void runBuilds(Set<String> changedComponentIds, Supplier<Boolean> shouldStop)
	{
		var projectsToBuild = getProjectsToBuild(changedComponentIds);
		for(var project : projectsToBuild)
			if(!shouldStop.get())
				projectBuilder.build(project, configuration);
		
		logger.info("Latest builds: {}", latestBuildRegistry.getReport());
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
				logger.info("Scheduling build of project '{}'. Changed project components: ({})",
						project.getId(), StringUtil.listToString(changedProjectComponentIds, ", "));
				projectsToBuild.add(project);
			}
		}
		
		return projectsToBuild;
	}
	
}
