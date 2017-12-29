package de.domisum.janusinfinifrons.storage;

import de.domisum.lib.auxilium.contracts.ToStringSerializer;
import de.domisum.lib.auxilium.contracts.storage.Storage;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class StringSerializedObjectStorage<StorageItemT> implements Storage<String, StorageItemT>
{

	// REFERENCES
	private final ToStringSerializer<StorageItemT> serializer;
	private final Storage<String, String> stringStorage;


	// STORAGE
	@Override public Optional<StorageItemT> fetch(String id)
	{
		Optional<String> serializedOptional = stringStorage.fetch(id);
		if(!serializedOptional.isPresent())
			return Optional.empty();

		return Optional.ofNullable(deserialize(serializedOptional.get()));
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
