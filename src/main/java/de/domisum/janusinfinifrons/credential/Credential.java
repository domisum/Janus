package de.domisum.janusinfinifrons.credential;

import de.domisum.lib.auxilium.contracts.storage.InMemoryCopyStorage;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class Credential implements InMemoryCopyStorage.Keyable<String>
{

	@Getter private final String id;

	@Getter private final String username;
	@Getter private final String password;


	@Override public String getKey()
	{
		return id;
	}

}
