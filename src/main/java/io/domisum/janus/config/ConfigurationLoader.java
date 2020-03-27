package io.domisum.janus.config;

import com.google.inject.Inject;
import io.domisum.janus.config.object.component.ComponentDependencyFacade;
import io.domisum.janus.config.object.component.ComponentLoader;
import io.domisum.janus.config.object.credentials.CredentialLoader;
import io.domisum.janus.config.object.project.ProjectLoader;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ConfigurationLoader
{
	
	// DEPENDENCIES
	private final CredentialLoader janusCredentialLoader;
	private final ComponentLoader janusComponentLoader;
	private final ProjectLoader janusProjectLoader;
	
	// DEPENDENCY FACADES
	private final ComponentDependencyFacade componentDependencyFacade;
	
	
	// LOAD
	public Configuration load()
			throws InvalidConfigurationException
	{
		var credentialRegistry = janusCredentialLoader.load();
		
		componentDependencyFacade.setCredentialRegistry(credentialRegistry);
		var componentRegistry = janusComponentLoader.load();
		
		// TODO project facade
		var projectRegistry = janusProjectLoader.load();
		
		return new Configuration(credentialRegistry, componentRegistry, projectRegistry);
	}
	
}
