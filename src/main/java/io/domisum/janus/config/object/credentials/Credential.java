package io.domisum.janus.config.object.credentials;

import io.domisum.janus.config.object.ConfigObject;
import io.domisum.janus.config.object.ValidationReport;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;

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
	public ValidationReport validate()
	{
		Validate.notNull(id, "id has to be set");
		Validate.notNull(username, "username has to be set");
		Validate.notNull(password, "password has to be set");
		
		return null;
	}
	
	
	// OBJECT
	@Override
	public String toString()
	{
		return getClass().getSimpleName()+"("+id+": "+username+")";
	}
	
}
