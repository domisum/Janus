package io.domisum.janus.config.object.project;

import io.domisum.janus.config.object.ConfigObjectLoader;
import io.domisum.lib.auxiliumlib.util.json.GsonUtil;

import java.util.Map;

public class ProjectLoader
		extends ConfigObjectLoader<Project>
{
	
	// CONSTANT METHODS
	@Override
	protected String OBJECT_NAME()
	{
		return "project";
	}
	
	@Override
	protected Project deserialize(String configContent)
	{
		return GsonUtil.get().fromJson(configContent, Project.class);
	}
	
	@Override
	protected Map<Class<?>,Object> getDependenciesToInject()
	{
		return null;
	}
	
}
