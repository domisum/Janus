package de.domisum.janusinfinifrons.storage;

import java.util.Collection;

/**
 * Interface describing the contract of a storage module. The purpose of the module is to store objects of the type
 * <code>StorageItemT</code> and be able to retrieve the instances later on. This contract does not guarantee persistence,
 * which could cause loss of stored data on shutdown. To check if this is the case, check the actual implementation of the
 * Storage module.
 * <p>
 * A storage module implementing this interface can only hold one item per id.
 *
 * @param <StorageItemT> the type of item to be stored
 */
public interface Storage<StorageItemT>
{

	/**
	 * Fetches the item associated with the given id. Iff the storage does not contain an item with the
	 * given id (iff {@link #contains(String)}</code> returns false), null is returned.
	 *
	 * @param id the id of the object to retrieve from storage
	 * @return the StorageItemT associated with the id, or null if none found
	 */
	StorageItemT fetch(String id);

	/**
	 * Fetches a collection of all StorageItemT stored by this storage module. If no items are stored, an empty collection is
	 * returned.
	 *
	 * @return a collection containing
	 */
	Collection<StorageItemT> fetchAll();


	/**
	 * Stores a <code>StorageItemT</code> in this module in order to fetch it later on. If the storage module already contains
	 * another item with the same id, it is discarded and the new item stored in its place.
	 *
	 * @param item the item to store
	 */
	void store(StorageItemT item);

	/**
	 * Checks if this storage module contains a <code>StorageItemT</code> with the id equal to the supplied id.
	 *
	 * @param id the id of the item to check against
	 * @return whether this storage module contains an item with the supplied id
	 */
	boolean contains(String id);


	/**
	 * Removes the item with the supplied id from the storage module.
	 *
	 * @param id the id of the item to remove
	 */
	void remove(String id);

}
