package io.domisum.janus.config.object.credentials;

import io.domisum.lib.auxiliumlib.config.ConfigObject;
import io.domisum.lib.auxiliumlib.config.ConfigException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Credential
		extends ConfigObject
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
			throws ConfigException
	{
		ConfigException.validateNotBlank(id, "id");
		ConfigException.validateNotBlank(username, "username");
		ConfigException.validateNotBlank(password, "password");
	}
	
	
	// OBJECT
	@Override
	public String toString()
	{
		return getClass().getSimpleName()+"("+id+": '"+username+"')";
	}
	
}
