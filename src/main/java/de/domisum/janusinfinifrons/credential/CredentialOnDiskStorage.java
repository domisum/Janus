package de.domisum.janusinfinifrons.credential;

import de.domisum.janusinfinifrons.storage.Storage;
import de.domisum.janusinfinifrons.storage.ondisk.StringOnDiskStorage;
import de.domisum.lib.auxilium.util.json.GsonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CredentialOnDiskStorage implements Storage<Credential>
{

	// CONSTANTS
	private static final String FILE_EXTENSION = "jns_cred";

	// REFERENCES
	private Storage<String> stringStorage;


	// INIT
	public CredentialOnDiskStorage(File credentialsDirectory)
	{
		stringStorage = new StringOnDiskStorage(credentialsDirectory, FILE_EXTENSION);
	}


	// STORAGE
	@Override public Credential fetch(String id)
	{
		return deserialize(stringStorage.fetch(id));
	}

	@Override public Collection<Credential> fetchAll()
	{
		List<Credential> credentials = new ArrayList<>();
		for(String cs : stringStorage.fetchAll())
			credentials.add(deserialize(cs));

		return credentials;
	}

	@Override public void store(Credential item)
	{
		throw new UnsupportedOperationException();
	}

	@Override public boolean contains(String id)
	{
		throw new UnsupportedOperationException();
	}

	@Override public void remove(String id)
	{
		throw new UnsupportedOperationException();
	}


	// SERIALIZATION
	private Credential deserialize(String credentialString)
	{
		return GsonUtil.get().fromJson(credentialString, Credential.class);
	}

}
