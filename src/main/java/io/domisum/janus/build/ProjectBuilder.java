package io.domisum.janus.build;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import io.domisum.janus.Janus;
import io.domisum.janus.api.JanusApiUsingFiles;
import io.domisum.janus.config.Configuration;
import io.domisum.janus.config.object.project.Project;
import io.domisum.lib.auxiliumlib.PHR;
import io.domisum.lib.auxiliumlib.config.ConfigException;
import io.domisum.lib.auxiliumlib.util.StringListUtil;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import io.domisum.lib.auxiliumlib.util.file.FileUtil.FileType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProjectBuilder
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// CONSTANTS
	private static final DateTimeFormatter BUILD_NAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_SSS")
		.withZone(ZoneId.systemDefault());
	
	
	// BUILD
	public boolean build(Project project, Configuration configuration)
		throws ConfigException
	{
		logger.info("Building project '{}'...", project.getId());
		
		boolean restart = false;
		if(project.getBuildRootDirectory() != null)
			buildRegular(project, configuration);
		else
			restart = buildAndExport(project, configuration);
		
		logger.info("...Building project '{}' done", project.getId());
		return restart;
	}
	
	private void buildRegular(Project project, Configuration configuration)
	{
		String buildName = createBuildName();
		logger.info("Build name: {}", buildName);
		
		var buildDirectory = new File(project.getBuildRootDirectory(), buildName);
		buildProjectTo(project, buildDirectory, configuration);
		
		var latestBuildFile = new File(project.getBuildRootDirectory(), Project.LATEST_BUILD_FILE_NAME);
		FileUtil.writeString(latestBuildFile, buildName);
	}
	
	private boolean buildAndExport(Project project, Configuration configuration)
		throws ConfigException
	{
		var tempBuildDir = FileUtil.createTemporaryDirectory();
		buildProjectTo(project, tempBuildDir, configuration);
		
		if(project.isJanusJar())
		{
			exportJanusJar(project, tempBuildDir);
			return true;
		}
		else if(project.isJanusConfig())
		{
			FileUtil.moveDirectory(Janus.CONFIG_DIRECTORY, Janus.CONFIG_DIRECTORY_BACKUP);
			exportBuild(tempBuildDir, Janus.CONFIG_DIRECTORY, false);
			return true;
		}
		else
		{
			exportBuild(tempBuildDir, project.getExportDirectory(), project.keepOtherFilesOnExport());
			return false;
		}
	}
	
	private void exportJanusJar(Project project, File tempBuildDir)
		throws ConfigException
	{
		if(FileUtil.listFilesFlat(tempBuildDir, FileType.DIRECTORY).size() > 0)
		{
			failBuild(project, "Build shouldn't contain any directories");
			return;
		}
		
		var buildFiles = FileUtil.listFilesFlat(tempBuildDir, FileType.FILE);
		if(buildFiles.isEmpty())
		{
			failBuild(project, "Build didn't contain a file");
			return;
		}
		if(buildFiles.size() > 1)
		{
			failBuild(project, "Build should not contain more than one file");
			return;
		}
		
		var buildJarFile = Iterables.getOnlyElement(buildFiles);
		if(!"jar".equals(FileUtil.getExtension(buildJarFile)))
		{
			failBuild(project, "File in build is not a jar file");
			return;
		}
		
		var targetFile = new File("Updated.jar");
		FileUtil.moveFile(buildJarFile, targetFile);
		FileUtil.deleteDirectory(tempBuildDir);
	}
	
	private void exportBuild(File tempBuildDir, File exportDirectory, boolean keepOtherFiles)
	{
		logger.info("Exporting build to dir '{}', keeping other files: {}", exportDirectory, keepOtherFiles);
		
		boolean clearOtherFiles = !keepOtherFiles;
		if(clearOtherFiles)
			FileUtil.deleteDirectoryContents(exportDirectory);
		
		FileUtil.moveDirectory(tempBuildDir, exportDirectory);
	}
	
	private void failBuild(Project project, String reason)
		throws ConfigException
	{
		throw new ConfigException(PHR.r("Build of project '{}' failed, reason: {}", project.getId(), reason));
	}
	
	
	private void buildProjectTo(Project project, File buildDirectory, Configuration configuration)
	{
		var componentRegistry = configuration.getComponentRegistry();
		
		var buildFingerprintParts = new ArrayList<String>();
		for(var projectComponent : project.getComponents())
		{
			var directoryInBuild = projectComponent.getDirectoryInBuild(buildDirectory);
			FileUtil.mkdirs(directoryInBuild);
			
			var component = componentRegistry.get(projectComponent.getComponentId());
			component.addToBuild(directoryInBuild);
			
			String buildFingerprintPart = component.getId()+":"+component.getFingerprint();
			buildFingerprintParts.add(buildFingerprintPart);
		}
		
		if(!project.isJanusConfig() && !project.isJanusJar())
		{
			Collections.sort(buildFingerprintParts);
			String buildFingerprint = StringListUtil.listHorizontally(buildFingerprintParts, "|");
			
			var buildFingerprintFile = new File(buildDirectory, JanusApiUsingFiles.BUILD_FINGERPRINT_FILE_NAME);
			FileUtil.writeString(buildFingerprintFile, buildFingerprint);
		}
	}
	
	
	// NAME
	private static String createBuildName()
	{
		return BUILD_NAME_DATE_TIME_FORMATTER.format(Instant.now());
	}
	
	public static Instant parseBuildTimeFromBuildDirectory(File buildDirectory)
	{
		try
		{
			var parsed = BUILD_NAME_DATE_TIME_FORMATTER.parse(buildDirectory.getName());
			return Instant.from(parsed);
		}
		catch(DateTimeParseException ignored)
		{
			return null;
		}
	}
	
}
