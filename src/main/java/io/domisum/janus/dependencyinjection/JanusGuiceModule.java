package io.domisum.janus.dependencyinjection;

import com.google.inject.AbstractModule;
import io.domisum.janus.component.JanusComponentDeserializer.Binding;
import io.domisum.janus.component.types.JanusComponentGitRepository;
import io.domisum.janus.component.types.JanusComponentMavenArtifactJar;
import io.domisum.lib.guiceutils.GuiceMultibinder;

public class JanusGuiceModule
		extends AbstractModule
{
	
	@Override
	protected void configure()
	{
		GuiceMultibinder.multibindInstances(binder(),
				Binding.class,
				
				new Binding("gitRepository", JanusComponentGitRepository.class),
				new Binding("mavenArtifactJar", JanusComponentMavenArtifactJar.class)
		);
	}
	
}
