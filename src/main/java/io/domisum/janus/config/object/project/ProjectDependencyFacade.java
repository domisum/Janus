package io.domisum.janus.config.object.project;

import com.google.inject.Singleton;
import io.domisum.janus.config.object.component.Component;
import io.domisum.lib.auxiliumlib.config.ConfigObjectRegistry;
import lombok.Setter;
import org.apache.commons.lang3.Validate;

@Singleton
public class ProjectDependencyFacade
{
	
	// DELEGATES
	@Setter
	private ConfigObjectRegistry<Component> componentRegistry = null;
	
	
	// COMPONENT
	public void validateComponentExists(String id)
	{
		Validate.isTrue(getComponentRegistry().contains(id), "there is no component with id "+id);
	}
	
	private ConfigObjectRegistry<Component> getComponentRegistry()
	{
		if(componentRegistry == null)
			throw new IllegalStateException("can't get component registry if it hasn't been set");
		return componentRegistry;
	}
	
}
