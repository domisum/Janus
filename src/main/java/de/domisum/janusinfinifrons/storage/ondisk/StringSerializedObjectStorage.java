package de.domisum.janusinfinifrons.storage.ondisk;

import de.domisum.lib.auxilium.contracts.storage.Storage;
import de.domisum.lib.auxilium.contracts.ToStringSerializer;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class StringSerializedObjectStorage<StorageItemT> implements Storage<StorageItemT>
{

	// REFERENCES
	private final ToStringSerializer<StorageItemT> serializer;
	private final Storage<String> stringStorage;


	// STORAGE
	@Override public StorageItemT fetch(String id)
	{
		return deserialize(stringStorage.fetch(id));
	}

	@Override public Collection<StorageItemT> fetchAll()
	{
		List<StorageItemT> items = new ArrayList<>();
		for(String cs : stringStorage.fetchAll())
			items.add(deserialize(cs));

		return items;
	}

	@Override public void store(StorageItemT item)
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
	private StorageItemT deserialize(String objectString)
	{
		return serializer.deserialize(objectString);
	}

}
