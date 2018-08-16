package de.domisum.janusinfinifrons;

import de.domisum.janusinfinifrons.build.ProjectBuild;
import de.domisum.janusinfinifrons.build.ProjectBuilder;
import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.janusinfinifrons.instance.JanusProjectInstance;
import de.domisum.janusinfinifrons.project.JanusProject;
import de.domisum.janusinfinifrons.project.ProjectComponentDependency;
import de.domisum.lib.auxilium.contracts.Identifyable;
import de.domisum.lib.auxilium.contracts.source.optional.FiniteOptionalSource;
import de.domisum.lib.auxilium.contracts.storage.Storage;
import de.domisum.lib.auxilium.util.FileUtil;
import de.domisum.lib.auxilium.util.FileUtil.FileType;
import de.domisum.lib.auxilium.util.ticker.Ticker;
import de.domisum.lib.auxilium.util.time.DurationUtil;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class UpdateTicker extends Ticker
{

	// CONSTANTS
	private static final Duration TICK_INTERVAL = Duration.ofSeconds(10);

	private static final Duration DELETE_INSTANCE_BUILDS_AFTER_INACTIVITY = Duration.ofDays(3);

	// REFERENCES
	private final FiniteOptionalSource<String, JanusComponent> componentSource;
	private final FiniteOptionalSource<String, JanusProject> projectSource;
	private final FiniteOptionalSource<String, JanusProjectInstance> projectInstanceSource;

	private final ProjectBuilder projectBuilder;
	private final Storage<JanusProject, ProjectBuild> latestBuilds;

	// STATUS
	private final Map<String, String> lastComponentVersions = new HashMap<>();


	// INIT
	public UpdateTicker(
			FiniteOptionalSource<String, JanusComponent> componentSource,
			FiniteOptionalSource<String, JanusProject> projectSource,
			FiniteOptionalSource<String, JanusProjectInstance> projectInstanceSource,
			ProjectBuilder projectBuilder,
			Storage<JanusProject, ProjectBuild> latestBuilds)
	{
		super(TICK_INTERVAL);
		this.componentSource = componentSource;
		this.projectSource = projectSource;
		this.projectInstanceSource = projectInstanceSource;

		this.projectBuilder = projectBuilder;
		this.latestBuilds = latestBuilds;

		logger.info("Starting ticker...");
		start();
	}


	// TICK
	@Override protected void tick()
	{
		Collection<JanusComponent> changedComponents = updateComponents();
		Collection<JanusProject> changedProjects = getChangedProjects(changedComponents);

		for(JanusProject jp : changedProjects)
			buildAndExportProject(jp);

		for(JanusProjectInstance projectInstance : projectInstanceSource.fetchAll())
			deleteOldBuildsIn(projectInstance);
	}

	private Collection<JanusComponent> updateComponents()
	{
		logger.debug("Updating components...");

		for(JanusComponent component : componentSource.fetchAll())
		{
			logger.debug("Updating component '{}'", component.getId());
			component.update();
		}

		Collection<JanusComponent> changedComponents = getCurrentlyChangedComponents();
		if(!changedComponents.isEmpty())
			logger.info("Detected change in components: {}", Identifyable.getIdList(changedComponents));

		// update last version
		for(JanusComponent c : componentSource.fetchAll())
			lastComponentVersions.put(c.getId(), c.getVersion());

		return changedComponents;
	}

	private void buildAndExportProject(JanusProject project)
	{
		logger.info("Starting build of project '{}'...", project.getId());
		ProjectBuild build = projectBuilder.build(project);

		exportProjectBuild(build);
		latestBuilds.store(build);

		build.delete();
	}

	private void exportProjectBuild(ProjectBuild build)
	{
		logger.info("Exporting build {}...", build);

		Collection<JanusProjectInstance> projectInstances = new ArrayList<>(projectInstanceSource.fetchAll());
		projectInstances.removeIf(i->!Objects.equals(i.getProjectId(), build.getProject().getId()));

		for(JanusProjectInstance instance : projectInstances)
			build.exportTo(instance);

		logger.info("Exporting build done\n");
	}

	private void deleteOldBuildsIn(JanusProjectInstance instance)
	{
		for(File buildDir : FileUtil.listFilesFlat(instance.getRootDirectory(), FileType.DIRECTORY))
		{
			Instant lastModified = FileUtil.getLastModified(buildDir);
			if(DurationUtil.isOlderThan(lastModified, DELETE_INSTANCE_BUILDS_AFTER_INACTIVITY))
			{
				logger.info("Deleting long unmodified build '{}' in instance '{}'", buildDir.getName(), instance.getId());
				FileUtil.deleteDirectory(buildDir);
			}
		}
	}


	// UTIL
	private Collection<JanusComponent> getCurrentlyChangedComponents()
	{
		Collection<JanusComponent> changedComponents = new ArrayList<>();

		for(JanusComponent c : componentSource.fetchAll())
			if(!Objects.equals(lastComponentVersions.get(c.getKey()), c.getVersion()))
				changedComponents.add(c);

		return changedComponents;
	}

	private Collection<JanusProject> getChangedProjects(Collection<JanusComponent> changedComponents)
	{
		Set<JanusProject> changedProjects = new HashSet<>();
		for(JanusProject project : projectSource.fetchAll())
			for(ProjectComponentDependency dependency : project.getComponentDependencies())
				for(JanusComponent changedComponent : changedComponents)
					if(Objects.equals(dependency.getComponentId(), changedComponent.getId()))
					{
						changedProjects.add(project);
						break;
					}

		if(!changedProjects.isEmpty())
			logger.info("Projects with changed components: {}", Identifyable.getIdList(changedProjects));

		return changedProjects;
	}

}
