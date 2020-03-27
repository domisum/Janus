package io.domisum.janus.config.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigObjectRegistry<T extends ConfigObject>
{
	
	// REGISTRY
	private final Map<String,T> configObjects;
	
	
	// INIT
	public ConfigObjectRegistry(Collection<T> configObjects)
	{
		var configObjectsMap = new HashMap<String,T>();
		
		for(T configObject : configObjects)
			configObjectsMap.put(configObject.getId(), configObject);
		
		this.configObjects = Collections.unmodifiableMap(configObjectsMap);
	}
	
	
	// REGISTRY
	public T get(String id)
	{
		T configObject = configObjects.get(id);
		if(configObject == null)
			throw new IllegalArgumentException("this registry doesn't contain an entry for id '"+id+"'");
		return configObject;
	}
	
	public Collection<T> getAll()
	{
		return new ArrayList<>(configObjects.values());
	}
	
}
