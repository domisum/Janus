package de.domisum.janusinfinifrons.credential;

import de.domisum.lib.auxilium.contracts.Identifyable;
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
