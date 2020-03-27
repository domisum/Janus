package io.domisum.janus.build;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import io.domisum.janus.Janus;
import io.domisum.janus.config.Configuration;
import io.domisum.janus.config.object.project.Project;
import io.domisum.lib.auxiliumlib.contracts.ApplicationStopper;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import io.domisum.lib.auxiliumlib.util.file.FileUtil.FileType;
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
	private final ApplicationStopper applicationStopper;
	
	
	// BUILD
	public void build(Project project, Configuration configuration)
	{
		logger.info("Building project '{}'...", project.getId());
		
		if(project.getBuildRootDirectory() != null)
			buildRegular(project, configuration);
		else
			buildAndExport(project, configuration);
		
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
	
	private void buildAndExport(Project project, Configuration configuration)
	{
		var tempBuildDir = FileUtil.createTemporaryDirectory();
		buildProjectTo(project, tempBuildDir, configuration);
		
		if(project.isJanusJar())
			exportJanusJar(project, tempBuildDir);
		else if(project.isJanusConfig())
		{
			exportBuild(tempBuildDir, Janus.CONFIG_DIRECTORY);
			applicationStopper.stop();
		}
		else
			exportBuild(tempBuildDir, project.getExportDirectory());
	}
	
	private void exportJanusJar(Project project, File tempBuildDir)
	{
		if(FileUtil.listFilesFlat(tempBuildDir, FileType.DIRECTORY).size() > 0)
		{
			failBuild(project, "build shouldn't contain any directories");
			return;
		}
		
		var buildFiles = FileUtil.listFilesFlat(tempBuildDir, FileType.FILE);
		if(buildFiles.isEmpty())
		{
			failBuild(project, "build didn't contain a file");
			return;
		}
		if(buildFiles.size() > 1)
		{
			failBuild(project, "build should not contain more than one file");
			return;
		}
		
		var buildJarFile = Iterables.getOnlyElement(buildFiles);
		if(!"jar".equals(FileUtil.getExtension(buildJarFile)))
		{
			failBuild(project, "file in build is not a jar file");
			return;
		}
		
		var targetFile = new File("Updated.jar");
		FileUtil.moveFile(buildJarFile, targetFile);
		FileUtil.deleteDirectory(tempBuildDir);
		applicationStopper.stop();
	}
	
	private void exportBuild(File tempBuildDir, File exportDirectory)
	{
		FileUtil.deleteDirectoryContents(exportDirectory);
		FileUtil.moveDirectory(tempBuildDir, exportDirectory);
	}
	
	private void failBuild(Project project, String reason)
	{
		logger.error("Build of project '{}' failed, reason: {}", project.getId(), reason);
		applicationStopper.stop();
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
