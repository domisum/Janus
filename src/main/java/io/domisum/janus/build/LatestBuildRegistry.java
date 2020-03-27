package io.domisum.janus.build;

import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class LatestBuildRegistry
{
	
	// REGISTRY
	private final Map<String,String> latestBuilds = new HashMap<>();
	
	
	// REGISTRY
	public void set(String projectId, String buildName)
	{
		latestBuilds.put(projectId, buildName);
	}
	
	public Optional<String> get(String projectId)
	{
		return Optional.ofNullable(latestBuilds.get(projectId));
	}
	
}
