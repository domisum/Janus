package io.domisum.janus.build;

import io.domisum.janus.config.object.project.Project;
import io.domisum.lib.auxiliumlib.util.DurationUtil;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import io.domisum.lib.auxiliumlib.util.file.FileUtil.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ProjectOldBuildsCleaner
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	// CONSTANTS
	private static final Duration MAX_BUILD_AGE = Duration.ofDays(7);
	private static final int MAX_BUILD_COUNT = 10;
	
	
	// CLEAN
	public void cleanOldBuilds(Project project)
	{
		if(project.getBuildRootDirectory() == null)
			return;
		
		String runningBuild = readRunningBuild(project.getBuildRootDirectory());
		deleteOldBuilds(project, runningBuild);
		deleteTooManyBuilds(project, runningBuild);
	}
	
	private void deleteOldBuilds(Project project, String runningBuild)
	{
		var buildDirectories = FileUtil.listFilesFlat(project.getBuildRootDirectory(), FileType.DIRECTORY);
		for(var buildDirectory : buildDirectories)
		{
			String buildName = buildDirectory.getName();
			var buildTime = parseBuildTimeFromBuildDirectory(buildDirectory);
			
			if(Objects.equals(buildName, runningBuild))
				continue;
			
			if(DurationUtil.isOlderThan(buildTime, MAX_BUILD_AGE))
			{
				logger.info("Deleting build {} of project {}: Exceeded max age", buildName, project.getId());
				FileUtil.deleteDirectory(buildDirectory);
			}
		}
	}
	
	private void deleteTooManyBuilds(Project project, String runningBuild)
	{
		var buildDirectories = FileUtil.listFilesFlat(project.getBuildRootDirectory(), FileType.DIRECTORY);
		if(buildDirectories.size() <= MAX_BUILD_COUNT)
			return;
		
		File directoryOfOldestBuild = null;
		var oldestBuildTime = Instant.now();
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
	
	
	// TODO move this to more appropriate place
	private Instant parseBuildTimeFromBuildDirectory(File buildDirectory)
	{
		var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_SSS").withZone(ZoneId.systemDefault());
		var parsed = dateTimeFormatter.parse(buildDirectory.getName());
		return Instant.from(parsed);
	}
	
}
