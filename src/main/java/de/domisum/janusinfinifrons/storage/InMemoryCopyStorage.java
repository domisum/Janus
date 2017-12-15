package de.domisum.janusinfinifrons.storage;

import de.domisum.lib.auxilium.contracts.Identifyable;
import de.domisum.lib.auxilium.contracts.storage.Storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class InMemoryCopyStorage<StorageItemT extends Identifyable> implements Storage<StorageItemT>
{

	// REFERENCES
	private Storage<StorageItemT> source;

	// TEMP
	private final Map<String, StorageItemT> items = new HashMap<>();


	// INIT
	public InMemoryCopyStorage(Storage<StorageItemT> source)
	{
		this.source = source;
	}

	public void loadFromSource()
	{
		items.clear();

		for(StorageItemT item : source.fetchAll())
			store(item);
	}


	// STORAGE
	@Override public StorageItemT fetch(String id)
	{
		return items.get(id);
	}

	@Override public Collection<StorageItemT> fetchAll()
	{
		return new ArrayList<>(items.values());
	}

	@Override public void store(StorageItemT item)
	{
		items.put(item.getId(), item);
	}

	@Override public boolean contains(String id)
	{
		return items.containsKey(id);
	}

	@Override public void remove(String id)
	{
		items.remove(id);
	}

}
