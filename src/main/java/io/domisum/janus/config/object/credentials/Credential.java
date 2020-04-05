package io.domisum.janus.config.object.credentials;

import io.domisum.lib.auxiliumlib.config.ConfigObject;
import io.domisum.lib.auxiliumlib.config.InvalidConfigException;
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
			throws InvalidConfigException
	{
		InvalidConfigException.validateNotBlank(id, "id");
		InvalidConfigException.validateNotBlank(username, "username");
		InvalidConfigException.validateNotBlank(password, "password");
	}
	
	
	// OBJECT
	@Override
	public String toString()
	{
		return getClass().getSimpleName()+"("+id+": '"+username+"')";
	}
	
}
