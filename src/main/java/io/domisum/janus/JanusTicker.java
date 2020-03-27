package io.domisum.janus;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.domisum.janus.build.ProjectOldBuildsCleaner;
import io.domisum.janus.config.Configuration;
import io.domisum.lib.auxiliumlib.ticker.Ticker;
import lombok.Setter;

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
		var changedComponentIds = new HashSet<String>();
		
		return changedComponentIds;
	}
	
	private void runBuilds(Set<String> changedComponentIds)
	{
	
	}
	
}
