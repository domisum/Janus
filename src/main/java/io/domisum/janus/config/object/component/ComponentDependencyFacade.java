package io.domisum.janus.config.object.component;

import com.google.inject.Singleton;
import io.domisum.janus.config.object.ConfigObjectRegistry;
import io.domisum.janus.config.object.credentials.Credential;
import lombok.Setter;
import org.apache.commons.lang3.Validate;

@Singleton
public class ComponentDependencyFacade
{
	
	// DELEGATES
	@Setter
	private ConfigObjectRegistry<Credential> credentialRegistry = null;
	
	
	// CREDENTIAL
	public void validateCredentialExists(String id)
	{
		Validate.isTrue(credentialRegistry.contains(id), "there is no credential with id "+id);
	}
	
	public Credential getCredential(String id)
	{
		return credentialRegistry.get(id);
	}
	
}
