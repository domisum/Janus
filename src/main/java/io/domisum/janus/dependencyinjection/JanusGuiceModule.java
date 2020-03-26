package io.domisum.janus.dependencyinjection;

import com.google.inject.AbstractModule;
import io.domisum.janus.component.JanusComponentLoader.Binding;
import io.domisum.janus.component.types.JanusComponentGitRepository;
import io.domisum.janus.component.types.JanusComponentMavenArtifactJar;
import io.domisum.janus.intercom.endpoints.JanusIntercomEndpointUpdateAvailable;
import io.domisum.lib.guiceutils.GuiceMultibinder;
import io.domisum.lib.httpbutler.HttpButlerEndpoint;

public class JanusGuiceModule
		extends AbstractModule
{
	
	@Override
	protected void configure()
	{
		GuiceMultibinder.multibindInstances(binder(), Binding.class,
				new Binding("gitRepository", JanusComponentGitRepository.class),
				new Binding("mavenArtifactJar", JanusComponentMavenArtifactJar.class)
		);
		
		GuiceMultibinder.multibind(binder(), HttpButlerEndpoint.class,
				JanusIntercomEndpointUpdateAvailable.class
		);
	}
	
}
