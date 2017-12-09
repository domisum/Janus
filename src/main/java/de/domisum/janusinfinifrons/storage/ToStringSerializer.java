package de.domisum.janusinfinifrons.storage;

public interface ToStringSerializer<T>
{

	String serialize(T object);

	T deserialize(String objectString);

}
