package io.domisum.janus.config;

import com.google.inject.Inject;
import io.domisum.janus.config.object.component.ComponentDependencyFacade;
import io.domisum.janus.config.object.component.ComponentLoader;
import io.domisum.janus.config.object.credentials.CredentialLoader;
import io.domisum.janus.config.object.project.ProjectDependencyFacade;
import io.domisum.janus.config.object.project.ProjectLoader;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ConfigurationLoader
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// DEPENDENCIES
	private final CredentialLoader janusCredentialLoader;
	private final ComponentLoader janusComponentLoader;
	private final ProjectLoader janusProjectLoader;
	
	// DEPENDENCY FACADES
	private final ComponentDependencyFacade componentDependencyFacade;
	private final ProjectDependencyFacade projectDependencyFacade;
	
	
	// LOAD
	public Configuration load()
			throws InvalidConfigurationException
	{
		logger.info("Loading configuration...");
		
		var credentialRegistry = janusCredentialLoader.load();
		
		componentDependencyFacade.setCredentialRegistry(credentialRegistry);
		var componentRegistry = janusComponentLoader.load();
		
		projectDependencyFacade.setComponentRegistry(componentRegistry);
		var projectRegistry = janusProjectLoader.load();
		
		var configuration = new Configuration(credentialRegistry, componentRegistry, projectRegistry);
		logger.info("...Loading configuration done\n");
		return configuration;
	}
	
}