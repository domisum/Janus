package de.domisum.janusinfinifrons.credential;

import de.domisum.lib.auxilium.contracts.storage.InMemoryProxyStorage;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class Credential implements InMemoryProxyStorage.Keyable<String>
{

	@Getter private final String id;

	@Getter private final String username;
	@Getter private final String password;


	@Override public String getKey()
	{
		return id;
	}


	public void validate()
	{

	}

}
