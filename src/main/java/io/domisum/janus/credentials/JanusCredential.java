package io.domisum.janus.credentials;

import io.domisum.janus.JanusConfigObject;
import io.domisum.janus.ValidationReport;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class JanusCredential
		implements JanusConfigObject
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
	
}
