package de.domisum.janusinfinifrons;

import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.lib.auxilium.contracts.Identifyable;
import de.domisum.lib.auxilium.contracts.source.FiniteSource;
import de.domisum.lib.auxilium.util.ticker.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class UpdateTicker extends Ticker
{

	private final Logger logger = LoggerFactory.getLogger(getClass());


	// CONSTANTS
	private static final Duration TICK_INTERVAL = Duration.ofSeconds(10);

	// STORAGE
	private final FiniteSource<String, JanusComponent> componentSource;

	// STATUS
	private final Map<String, String> lastComponentVersions = new HashMap<>();


	// INIT
	public UpdateTicker(FiniteSource<String, JanusComponent> componentSource)
	{
		super(TICK_INTERVAL, "updateTicker");
		this.componentSource = componentSource;

		start();
	}


	// TICK
	@Override protected void tick()
	{
		Collection<JanusComponent> changedComponents = updateComponents();
	}

	private Collection<JanusComponent> updateComponents()
	{
		for(JanusComponent component : componentSource.fetchAll())
		{
			logger.info("Updating component '{}'", component.getId());
			component.update();
		}

		Collection<JanusComponent> changedComponents = getCurrentlyChangedComponents();
		logger.info("Changed components: {}", Identifyable.getIdList(changedComponents));

		// update last version
		for(JanusComponent c : componentSource.fetchAll())
			lastComponentVersions.put(c.getId(), c.getVersion());

		return changedComponents;
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

}
