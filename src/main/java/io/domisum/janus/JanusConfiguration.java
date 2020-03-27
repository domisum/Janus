package io.domisum.janus;

import io.domisum.janus.configobject.JanusConfigObjectRegistry;
import io.domisum.janus.configobject.component.JanusComponent;
import io.domisum.janus.configobject.credentials.JanusCredential;
import io.domisum.janus.configobject.project.JanusProject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JanusConfiguration
{
	
	private final JanusConfigObjectRegistry<JanusCredential> credentialRegistry;
	private final JanusConfigObjectRegistry<JanusComponent> componentRegistry;
	private final JanusConfigObjectRegistry<JanusProject> projectRegistry;
	
}
