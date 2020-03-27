package io.domisum.janus;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.domisum.janus.build.ProjectOldBuildsCleaner;
import io.domisum.janus.config.Configuration;
import io.domisum.lib.auxiliumlib.ticker.Ticker;
import lombok.Setter;

import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@Singleton
public class JanusTicker
		extends Ticker
{
	
	// DEPENDENCIES
	private final ProjectOldBuildsCleaner projectOldBuildsCleaner;
	
	// CONFIG
	@Setter
	private transient Configuration configuration = null;
	
	
	// INIT
	@Inject
	public JanusTicker(ProjectOldBuildsCleaner projectOldBuildsCleaner)
	{
		super("janusTicker", Duration.ofSeconds(5), Duration.ofMinutes(5));
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
		cleanOldBuilds();
		
		var updatedComponentIds = updateComponents();
		runBuilds(updatedComponentIds);
	}
	
	private void cleanOldBuilds()
	{
		var projects = configuration.getProjectRegistry().getAll();
		for(var project : projects)
			projectOldBuildsCleaner.cleanOldBuilds(project);
	}
	
	
	private Set<String> updateComponents()
	{
		try
		{
			return updateComponentsUncaught();
		}
		catch(IOException e)
		{
			logger.warn("Failed to update component", e);
			return new HashSet<>(); // don't trigger builds after failed update
		}
	}
	
	private Set<String> updateComponentsUncaught()
			throws IOException
	{
		var changedComponentIds = new HashSet<String>();
		
		var components = configuration.getComponentRegistry().getAll();
		for(var component : components)
		{
			boolean changed = component.update();
			if(changed)
				changedComponentIds.add(component.getId());
		}
		
		return changedComponentIds;
	}
	
	private void runBuilds(Set<String> changedComponentIds)
	{
	
	}
	
}
