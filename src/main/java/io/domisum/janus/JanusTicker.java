package io.domisum.janus;

import com.google.inject.Singleton;
import io.domisum.janus.config.Configuration;
import io.domisum.janus.config.object.project.Project;
import io.domisum.lib.auxiliumlib.ticker.Ticker;
import io.domisum.lib.auxiliumlib.util.DurationUtil;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import io.domisum.lib.auxiliumlib.util.file.FileUtil.FileType;
import lombok.Setter;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

@Singleton
public class JanusTicker
		extends Ticker
{
	
	// CONFIG
	@Setter
	private transient Configuration configuration = null;
	
	
	// INIT
	public JanusTicker()
	{
		super("janusTicker", Duration.ofSeconds(5), Duration.ofMinutes(5));
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
		deleteOldBuilds();
		
		var updatedComponentIds = updateComponents();
		runBuilds(updatedComponentIds);
	}
	
	private void deleteOldBuilds()
	{
		var projects = configuration.getProjectRegistry().getAll();
		for(var project : projects)
			deleteOldBuilds(project);
	}
	
	private void deleteOldBuilds(Project project)
	{
		if(project.getBuildRootDirectory() == null)
			return;
		
		String runningBuild = readRunningBuild(project.getBuildRootDirectory());
		deleteOldBuilds(project, runningBuild);
		deleteTooManyBuilds(project, runningBuild);
	}
	
	private void deleteOldBuilds(Project project, String runningBuild)
	{
		final Duration maxAge = Duration.ofDays(7);
		
		var buildDirectories = FileUtil.listFilesFlat(project.getBuildRootDirectory(), FileType.DIRECTORY);
		for(var buildDirectory : buildDirectories)
		{
			String buildName = buildDirectory.getName();
			var buildTime = parseBuildTimeFromBuildDirectory(buildDirectory);
			
			if(Objects.equals(buildName, runningBuild))
				continue;
			
			if(DurationUtil.isOlderThan(buildTime, maxAge))
			{
				logger.info("Deleting build {} of project {}: Exceeded max age", buildName, project.getId());
				FileUtil.deleteDirectory(buildDirectory);
			}
		}
	}
	
	private void deleteTooManyBuilds(Project project, String runningBuild)
	{
		final int maxCount = 10;
		
		var buildDirectories = FileUtil.listFilesFlat(project.getBuildRootDirectory(), FileType.DIRECTORY);
		if(buildDirectories.size() <= maxCount)
			return;
		
		File directoryOfOldestBuild = null;
		Instant oldestBuildTime = Instant.now();
		for(var buildDirectory : buildDirectories)
		{
			if(Objects.equals(buildDirectory.getName(), runningBuild))
				continue;
			
			var buildTime = parseBuildTimeFromBuildDirectory(buildDirectory);
			if(buildTime.compareTo(oldestBuildTime) < 0)
			{
				directoryOfOldestBuild = buildDirectory;
				oldestBuildTime = buildTime;
			}
		}
		
		logger.info("Deleting build {} of project {}: Too many builds", directoryOfOldestBuild.getName(), project.getId());
		FileUtil.deleteDirectory(directoryOfOldestBuild);
	}
	
	private String readRunningBuild(File buildRootDirectory)
	{
		var runningBuildFile = new File(buildRootDirectory, "runningBuild.txt");
		if(!runningBuildFile.exists())
			return null;
		return FileUtil.readString(runningBuildFile);
	}
	
	
	private Set<String> updateComponents()
	{
		var changedComponentIds = new HashSet<String>();
		
		return changedComponentIds;
	}
	
	private void runBuilds(Set<String> changedComponentIds)
	{
	
	}
	
	
	// TODO move this to more appropriate place
	private Instant parseBuildTimeFromBuildDirectory(File buildDirectory)
	{
		var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_SSS").withZone(ZoneId.systemDefault());
		var parsed = dateTimeFormatter.parse(buildDirectory.getName());
		return Instant.from(parsed);
	}
	
}
