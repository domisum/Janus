package io.domisum.janus.build;

import com.google.inject.Inject;
import io.domisum.janus.config.object.project.Project;
import io.domisum.lib.auxiliumlib.util.Compare;
import io.domisum.lib.auxiliumlib.util.FileUtil;
import io.domisum.lib.auxiliumlib.util.FileUtil.FileType;
import io.domisum.lib.auxiliumlib.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
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
		
		String runningBuild = readRunningBuild(project);
		deleteOldBuilds(project, runningBuild);
		deleteTooManyBuilds(project, runningBuild);
	}
	
	private void deleteOldBuilds(Project project, String runningBuild)
	{
		String latestBuild = readLatestBuild(project);
		
		var buildDirectories = FileUtil.listFilesFlat(project.getBuildRootDirectory(), FileType.DIRECTORY);
		for(var buildDirectory : buildDirectories)
		{
			String buildName = buildDirectory.getName();
			var buildInstantOptional = ProjectBuilder.parseBuildInstantFromBuildDirectory(buildDirectory);
			if(buildInstantOptional.isEmpty())
			{
				logger.warn("Failed to parse build time of directory in build root directory: {}", buildDirectory);
				continue;
			}
			var buildInstant = buildInstantOptional.get();
			
			if(Objects.equals(buildName, runningBuild))
				continue;
			if(Objects.equals(buildName, latestBuild))
				continue;
			
			if(TimeUtil.isOlderThan(buildInstant, MAX_BUILD_AGE))
			{
				logger.info("Deleting build '{}' of project '{}': Older than max build age", buildName, project.getId());
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
		var oldestBuildInstant = Instant.now();
		for(var buildDirectory : buildDirectories)
		{
			if(Objects.equals(buildDirectory.getName(), runningBuild))
				continue;
			
			var buildInstantOptional = ProjectBuilder.parseBuildInstantFromBuildDirectory(buildDirectory);
			if(buildInstantOptional.isEmpty())
			{
				logger.warn("Failed to parse build time of directory in build root directory: {}", buildDirectory);
				continue;
			}
			var buildInstant = buildInstantOptional.get();
			
			if(Compare.lessThan(buildInstant, oldestBuildInstant))
			{
				directoryOfOldestBuild = buildDirectory;
				oldestBuildInstant = buildInstant;
			}
		}
		
		logger.info("Deleting build '{}' of project '{}': Too many builds in directory", directoryOfOldestBuild.getName(), project.getId());
		FileUtil.deleteDirectory(directoryOfOldestBuild);
	}
	
	private String readLatestBuild(Project project)
	{
		var runningBuildFile = new File(project.getBuildRootDirectory(), Project.LATEST_BUILD_FILE_NAME);
		if(!runningBuildFile.exists())
			return null;
		return FileUtil.readString(runningBuildFile);
	}
	
	private String readRunningBuild(Project project)
	{
		var runningBuildFile = new File(project.getBuildRootDirectory(), Project.RUNNINTG_BUILD_FILE_NAME);
		if(!runningBuildFile.exists())
			return null;
		return FileUtil.readString(runningBuildFile);
	}
	
}
