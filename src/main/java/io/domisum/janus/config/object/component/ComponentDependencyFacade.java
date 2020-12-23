package io.domisum.janus.config.object.component;

import com.google.inject.Singleton;
import io.domisum.janus.config.object.credentials.Credential;
import io.domisum.lib.auxiliumlib.config.ConfigException;
import io.domisum.lib.auxiliumlib.config.ConfigObjectRegistry;
import lombok.Setter;

@Singleton
public class ComponentDependencyFacade
{
	
	// DELEGATES
	@Setter
	private ConfigObjectRegistry<Credential> credentialRegistry = null;
	
	
	// CREDENTIAL
	public void validateCredentialExists(String id)
		throws ConfigException
	{
		ConfigException.validateIsTrue(getCredentialRegistry().contains(id), "there is no credential with id "+id);
	}
	
	public Credential getCredential(String id)
	{
		return getCredentialRegistry().get(id);
	}
	
	private ConfigObjectRegistry<Credential> getCredentialRegistry()
	{
		if(credentialRegistry == null)
			throw new IllegalStateException("Can't get credential registry if it hasn't been set");
		return credentialRegistry;
	}
	
}
