package io.domisum.janus.config;

import com.google.inject.Inject;
import io.domisum.janus.config.object.component.ComponentDependencyFacade;
import io.domisum.janus.config.object.component.ComponentLoader;
import io.domisum.janus.config.object.credentials.CredentialLoader;
import io.domisum.janus.config.object.project.ProjectDependencyFacade;
import io.domisum.janus.config.object.project.ProjectLoader;
import io.domisum.lib.auxiliumlib.config.ConfigException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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
	public Configuration load(File configDirectory)
			throws ConfigException
	{
		logger.info("Loading configuration from directory '{}'...", configDirectory);
		
		var credentialRegistry = janusCredentialLoader.load(configDirectory);
		
		componentDependencyFacade.setCredentialRegistry(credentialRegistry);
		var componentRegistry = janusComponentLoader.load(configDirectory);
		
		projectDependencyFacade.setComponentRegistry(componentRegistry);
		var projectRegistry = janusProjectLoader.load(configDirectory);
		
		var configuration = new Configuration(credentialRegistry, componentRegistry, projectRegistry);
		logger.info("...Loading configuration done\n");
		return configuration;
	}
	
}
