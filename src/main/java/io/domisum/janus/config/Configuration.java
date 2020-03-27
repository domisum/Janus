package io.domisum.janus.config;

import io.domisum.janus.config.object.ConfigObjectRegistry;
import io.domisum.janus.config.object.component.Component;
import io.domisum.janus.config.object.credentials.Credential;
import io.domisum.janus.config.object.project.Project;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Configuration
{
	
	private final ConfigObjectRegistry<Credential> credentialRegistry;
	private final ConfigObjectRegistry<Component> componentRegistry;
	private final ConfigObjectRegistry<Project> projectRegistry;
	
}
