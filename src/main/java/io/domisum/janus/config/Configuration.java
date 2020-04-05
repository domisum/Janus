package io.domisum.janus.config;

import io.domisum.janus.config.object.component.Component;
import io.domisum.janus.config.object.credentials.Credential;
import io.domisum.janus.config.object.project.Project;
import io.domisum.lib.auxiliumlib.config.ConfigObjectRegistry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Configuration
{
	
	@Getter
	private final ConfigObjectRegistry<Credential> credentialRegistry;
	@Getter
	private final ConfigObjectRegistry<Component> componentRegistry;
	@Getter
	private final ConfigObjectRegistry<Project> projectRegistry;
	
}
