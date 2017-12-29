package de.domisum.janusinfinifrons;

import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.lib.auxilium.contracts.source.FiniteSource;
import de.domisum.lib.auxilium.util.ticker.Ticker;

import java.time.Duration;

public final class UpdateTicker extends Ticker
{

	// CONSTANTS
	private static final Duration TICK_INTERVAL = Duration.ofSeconds(10);

	// STORAGE
	private final FiniteSource<String, JanusComponent> componentSource;


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

	}

}
