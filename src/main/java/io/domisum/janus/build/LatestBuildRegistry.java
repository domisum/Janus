package io.domisum.janus.build;

import com.google.inject.Singleton;
import io.domisum.lib.auxiliumlib.util.StringUtil;

import java.util.ArrayList;
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
	
	public String getReport()
	{
		var latestBuildsKeyValuePairs = new ArrayList<String>();
		for(var latestBuildsEntry : latestBuilds.entrySet())
			latestBuildsKeyValuePairs.add(latestBuildsEntry.getKey()+"="+latestBuildsEntry.getValue());
		
		var report = latestBuildsKeyValuePairs.isEmpty() ? "none" : StringUtil.listToString(latestBuildsKeyValuePairs, ", ");
		return "("+report+")";
	}
	
}
