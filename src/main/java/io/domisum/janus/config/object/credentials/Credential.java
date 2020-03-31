package io.domisum.janus.config.object.credentials;

import io.domisum.janus.config.object.ConfigObject;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class Credential
		implements ConfigObject
{
	
	@Getter
	private final String id;
	@Getter
	private final String username;
	@Getter
	private final String password;
	
	
	// INIT
	@Override
	public void validate()
			throws InvalidConfigurationException
	{
		InvalidConfigurationException.validateIsSet(id, "id");
		InvalidConfigurationException.validateIsSet(username, "username");
		InvalidConfigurationException.validateIsSet(password, "password");
	}
	
	
	// OBJECT
	@Override
	public String toString()
	{
		return getClass().getSimpleName()+"("+id+": '"+username+"')";
	}
	
}
