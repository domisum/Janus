package io.domisum.janus.config.object.component;

import com.google.inject.Singleton;
import io.domisum.janus.config.object.ConfigObjectRegistry;
import io.domisum.janus.config.object.credentials.Credential;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import lombok.Setter;

@Singleton
public class ComponentDependencyFacade
{
	
	// DELEGATES
	@Setter
	private ConfigObjectRegistry<Credential> credentialRegistry = null;
	
	
	// CREDENTIAL
	public void validateCredentialExists(String id)
			throws InvalidConfigurationException
	{
		InvalidConfigurationException.validateIsTrue(getCredentialRegistry().contains(id), "there is no credential with id "+id);
	}
	
	public Credential getCredential(String id)
	{
		return getCredentialRegistry().get(id);
	}
	
	private ConfigObjectRegistry<Credential> getCredentialRegistry()
	{
		if(credentialRegistry == null)
			throw new IllegalStateException("can't get credential registry if it hasn't been set");
		return credentialRegistry;
	}
	
}
