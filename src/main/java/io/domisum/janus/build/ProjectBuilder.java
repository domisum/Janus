package io.domisum.janus.build;

import com.google.inject.Inject;
import io.domisum.janus.config.Configuration;
import io.domisum.janus.config.object.project.Project;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProjectBuilder
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// CONSTANTS
	private static final DateTimeFormatter BUILD_NAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_SSS")
			.withZone(ZoneId.systemDefault());
	
	// DEPENDENCIES
	private final LatestBuildRegistry latestBuildRegistry;
	
	
	// BUILD
	public void build(Project project, Configuration configuration)
	{
		logger.info("Building project '{}'...", project.getId());
		
		if(project.getBuildRootDirectory() != null)
			buildRegular(project, configuration);
		else
			buildExport(project, configuration);
		
		logger.info("...Building project '{}' done", project.getId());
	}
	
	private void buildRegular(Project project, Configuration configuration)
	{
		String buildName = createBuildName();
		
		var buildDirectory = new File(project.getBuildRootDirectory(), buildName);
		buildProjectTo(project, buildDirectory, configuration);
		
		latestBuildRegistry.set(project.getId(), buildName);
		var latestBuildFile = new File(project.getBuildRootDirectory(), Project.LATEST_BUILD_FILE_NAME);
		FileUtil.writeString(latestBuildFile, buildName);
	}
	
	private void buildExport(Project project, Configuration configuration)
	{
		// TODO
	}
	
	private void buildProjectTo(Project project, File buildDirectory, Configuration configuration)
	{
		var componentRegistry = configuration.getComponentRegistry();
		
		for(var projectComponent : project.getComponents())
		{
			var directoryInBuild = projectComponent.getDirectoryInBuild(buildDirectory);
			FileUtil.mkdirs(directoryInBuild);
			
			var component = componentRegistry.get(projectComponent.getComponentId());
			component.addToBuild(directoryInBuild);
		}
	}
	
	
	// NAME
	private static String createBuildName()
	{
		return BUILD_NAME_DATE_TIME_FORMATTER.format(Instant.now());
	}
	
	public static Instant parseBuildTimeFromBuildDirectory(File buildDirectory)
	{
		var parsed = BUILD_NAME_DATE_TIME_FORMATTER.parse(buildDirectory.getName());
		return Instant.from(parsed);
	}
	
}
