package de.domisum.janusinfinifrons.build;

import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.janusinfinifrons.project.JanusProject;
import de.domisum.janusinfinifrons.storage.Source;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ProjectBuilder
{

	private final File baseDirectory;
	private final Source<JanusComponent> componentSource;


	// BUILD
	public ProjectBuild build(JanusProject project)
	{
		Instant now = Instant.now();
		ProjectBuild build = new ProjectBuild(project, now, createBuildDirectory(project, now));

		List<JanusComponent> components = getProjectComponents(project);
		for(JanusComponent component : components)
			component.addToBuild(build);

		return build;
	}

	private File createBuildDirectory(JanusProject project, Instant time)
	{
		String directoryName = project.getId()+"-"+time.toEpochMilli();

		File buildDirectory = new File(baseDirectory, directoryName);
		buildDirectory.mkdir();

		return buildDirectory;
	}

	private List<JanusComponent> getProjectComponents(JanusProject project)
	{
		List<JanusComponent> components = new ArrayList<>();
		for(String componentId : project.getComponentIds())
		{
			JanusComponent component = this.componentSource.fetch(componentId);
			components.add(component);
		}

		return components;
	}

}
