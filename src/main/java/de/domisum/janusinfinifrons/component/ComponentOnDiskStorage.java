package de.domisum.janusinfinifrons.component;

import de.domisum.janusinfinifrons.storage.Storage;
import de.domisum.janusinfinifrons.storage.StringOnDiskStorage;
import de.domisum.lib.auxilium.util.json.GsonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ComponentOnDiskStorage implements Storage<JanusComponent>
{

	// CONSTANTS
	private static final String FILE_EXTENSION = "jns_comp";

	// REFERENCES
	private Storage<String> stringStorage;


	// INIT
	public ComponentOnDiskStorage(File credentialsDirectory)
	{
		stringStorage = new StringOnDiskStorage(credentialsDirectory, FILE_EXTENSION);
	}


	// STORAGE
	@Override public JanusComponent fetch(String id)
	{
		return deserialize(stringStorage.fetch(id));
	}

	@Override public Collection<JanusComponent> fetchAll()
	{
		List<JanusComponent> components = new ArrayList<>();
		for(String cs : stringStorage.fetchAll())
			components.add(deserialize(cs));

		return components;
	}

	@Override public void store(JanusComponent item)
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
	private JanusComponent deserialize(String credentialString)
	{
		// TODO
		return GsonUtil.get().fromJson(credentialString, JanusComponent.class);
	}

}
