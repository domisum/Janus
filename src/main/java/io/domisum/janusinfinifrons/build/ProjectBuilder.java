package io.domisum.janusinfinifrons.build;

import io.domisum.janusinfinifrons.component.JanusComponent;
import io.domisum.janusinfinifrons.project.JanusProject;
import io.domisum.janusinfinifrons.project.ProjectComponentDependency;
import io.domisum.lib.auxiliumlib.contracts.source.optional.FiniteOptionalSource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
public class ProjectBuilder
{

	private final Logger logger = LoggerFactory.getLogger(getClass());


	// REFERENCES
	private final File baseDirectory;
	private final FiniteOptionalSource<String, JanusComponent> componentSource;


	// BUILD
	public ProjectBuild build(JanusProject project)
	{
		String buildName = getBuildName(project);
		ProjectBuild build = new ProjectBuild(project, buildName, createBuildDirectory(project, buildName));
		addComponentsToBuild(project, build);

		logger.info("Build done");
		return build;
	}

	private void addComponentsToBuild(JanusProject project, ProjectBuild build)
	{
		for(ProjectComponentDependency dependency : project.getComponentDependencies())
		{
			JanusComponent component = componentSource.fetchOrException(dependency.getComponentId());
			logger.info("Adding component '{}' to build", component.getId());
			component.addToBuildThrough(dependency, build);
		}
	}

	private File createBuildDirectory(JanusProject project, String buildName)
	{
		File projectBuildDirectory = new File(baseDirectory, project.getId());
		File buildDirectory = new File(projectBuildDirectory, buildName);

		buildDirectory.mkdir();
		logger.info("Building into '{}'", buildDirectory);
		return buildDirectory;
	}


	// UTIL
	private static String getBuildName(JanusProject project)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'-HH-mm-ss.SSS'Z'").withZone(ZoneId.of("UTC"));
		String timestamp = formatter.format(Instant.now());

		return project.getId()+"#"+timestamp;
	}

}
