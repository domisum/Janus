package de.domisum.janusinfinifrons.component;

import java.io.File;

public class JanusComponentUpdater
{

	private final JanusComponent component;

	// INIT
	public JanusComponentUpdater(File baseDirectory, JanusComponent component)
	{
		this.component = component;

		component.setHelperDirectory(new File(baseDirectory, component.getId()));
	}


	// UPDATE
	public void update()
	{
		component.update();
		System.out.println(component.getVersion());
	}

}
