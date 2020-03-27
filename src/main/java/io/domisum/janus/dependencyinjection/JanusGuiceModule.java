package io.domisum.janus.dependencyinjection;

import com.google.inject.AbstractModule;
import io.domisum.janus.Janus;
import io.domisum.janus.config.object.component.ComponentLoader.Binding;
import io.domisum.janus.config.object.component.types.ComponentGitRepository;
import io.domisum.janus.config.object.component.types.ComponentMavenArtifactJar;
import io.domisum.janus.intercom.endpoints.IntercomEndpointUpdateAvailable;
import io.domisum.lib.auxiliumlib.contracts.ApplicationStopper;
import io.domisum.lib.guiceutils.GuiceMultibinder;
import io.domisum.lib.httpbutler.HttpButlerEndpoint;

public class JanusGuiceModule
		extends AbstractModule
{
	
	@Override
	protected void configure()
	{
		GuiceMultibinder.multibindInstances(binder(), Binding.class,
				new Binding("gitRepository", ComponentGitRepository.class),
				new Binding("mavenArtifactJar", ComponentMavenArtifactJar.class)
		);
		
		GuiceMultibinder.multibind(binder(), HttpButlerEndpoint.class,
				IntercomEndpointUpdateAvailable.class
		);
		
		bind(ApplicationStopper.class).to(Janus.class);
	}
	
}
