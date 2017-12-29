package de.domisum.janusinfinifrons;

import de.domisum.janusinfinifrons.build.ProjectBuild;
import de.domisum.janusinfinifrons.build.ProjectBuilder;
import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.janusinfinifrons.project.JanusProject;
import de.domisum.lib.auxilium.contracts.Identifyable;
import de.domisum.lib.auxilium.contracts.source.FiniteSource;
import de.domisum.lib.auxilium.util.ticker.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public final class UpdateTicker extends Ticker
{

	private final Logger logger = LoggerFactory.getLogger(getClass());


	// CONSTANTS
	private static final Duration TICK_INTERVAL = Duration.ofSeconds(10);

	// REFERENCES
	private final FiniteSource<String, JanusComponent> componentSource;
	private final FiniteSource<String, JanusProject> projectSource;
	private final ProjectBuilder projectBuilder;

	// STATUS
	private final Map<String, String> lastComponentVersions = new HashMap<>();


	// INIT
	public UpdateTicker(
			FiniteSource<String, JanusComponent> componentSource,
			FiniteSource<String, JanusProject> projectSource,
			ProjectBuilder projectBuilder)
	{
		super(TICK_INTERVAL);
		this.componentSource = componentSource;
		this.projectSource = projectSource;
		this.projectBuilder = projectBuilder;

		logger.info("Starting ticker...");
		start();
	}


	// TICK
	@Override protected void tick()
	{
		Collection<JanusComponent> changedComponents = updateComponents();
		Collection<JanusProject> changedProjects = getChangedProjects(changedComponents);

		for(JanusProject jp : changedProjects)
			buildProject(jp);
	}

	private Collection<JanusComponent> updateComponents()
	{
		for(JanusComponent component : componentSource.fetchAll())
		{
			logger.debug("Updating component '{}'", component.getId());
			component.update();
		}

		Collection<JanusComponent> changedComponents = getCurrentlyChangedComponents();
		if(!changedComponents.isEmpty())
			logger.info("Changed components: {}", Identifyable.getIdList(changedComponents));

		// update last version
		for(JanusComponent c : componentSource.fetchAll())
			lastComponentVersions.put(c.getId(), c.getVersion());

		return changedComponents;
	}

	private void buildProject(JanusProject project)
	{
		logger.info("Starting build of project '{}'...", project.getId());
		ProjectBuild build = projectBuilder.build(project);
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
		Collection<JanusProject> changedProjects = new HashSet<>();
		for(JanusComponent jc : changedComponents)
			for(JanusProject jp : projectSource.fetchAll())
				if(jp.getComponentIds().contains(jc.getId()))
				{
					changedProjects.add(jp);
					break;
				}

		if(!changedProjects.isEmpty())
			logger.info("Changed projects: {}", Identifyable.getIdList(changedProjects));

		return changedProjects;
	}

}
