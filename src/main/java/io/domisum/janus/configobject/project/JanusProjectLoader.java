package io.domisum.janus.configobject.project;

import io.domisum.janus.configobject.JanusConfigObjectLoader;
import io.domisum.lib.auxiliumlib.util.json.GsonUtil;

import java.util.Map;

public class JanusProjectLoader
		extends JanusConfigObjectLoader<JanusProject>
{
	
	// CONSTANT METHODS
	@Override
	protected String OBJECT_NAME()
	{
		return "project";
	}
	
	@Override
	protected JanusProject deserialize(String configContent)
	{
		return GsonUtil.get().fromJson(configContent, JanusProject.class);
	}
	
	@Override
	protected Map<Class<?>,Object> getDependenciesToInject()
	{
		return null;
	}
	
}
