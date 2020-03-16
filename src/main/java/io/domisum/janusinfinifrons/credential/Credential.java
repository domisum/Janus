package io.domisum.janusinfinifrons.credential;

import io.domisum.lib.auxiliumlib.contracts.Identifyable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class Credential implements Identifyable
{

	@Getter private final String id;

	@Getter private final String username;
	@Getter private final String password;


	public void validate()
	{
		// nothing to validate currently
	}

}
