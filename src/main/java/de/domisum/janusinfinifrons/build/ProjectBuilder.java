package de.domisum.janusinfinifrons.build;

import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.janusinfinifrons.project.JanusProject;
import de.domisum.lib.auxilium.contracts.source.FiniteSource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ProjectBuilder
{

	private final Logger logger = LoggerFactory.getLogger(getClass());


	// REFERENCES
	private final File baseDirectory;
	private final FiniteSource<String, JanusComponent> componentSource;


	// BUILD
	public ProjectBuild build(JanusProject project)
	{
		Instant now = Instant.now();
		String buildName = getBuildName(now);
		ProjectBuild build = new ProjectBuild(project, buildName, createBuildDirectory(project, buildName));

		List<JanusComponent> components = getProjectComponents(project);
		for(JanusComponent component : components)
		{
			logger.info("Adding component '{}' to build", component.getId());
			component.addToBuild(build);
		}

		logger.info("Build done\n");
		return build;
	}

	private File createBuildDirectory(JanusProject project, String buildName)
	{
		File projectBuildDirectory = new File(baseDirectory, project.getId());
		File buildDirectory = new File(projectBuildDirectory, buildName);

		buildDirectory.mkdir();
		logger.info("Building into '{}'", buildDirectory);
		return buildDirectory;
	}

	private List<JanusComponent> getProjectComponents(JanusProject project)
	{
		List<JanusComponent> components = new ArrayList<>();
		for(String componentId : project.getComponentIds())
		{
			JanusComponent component = componentSource.fetchOrException(componentId);
			components.add(component);
		}

		return components;
	}


	// UTIL
	private static String getBuildName(Instant time)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'-HH-mm-ss.SSS'Z'").withZone(ZoneId.of("UTC"));
		return formatter.format(time);
	}

}
