package io.domisum.janus.credentials;

import io.domisum.janus.JanusConfigObjectLoader;
import io.domisum.lib.auxiliumlib.util.json.GsonUtil;

import java.util.HashMap;
import java.util.Map;

public class JanusCredentialLoader
		extends JanusConfigObjectLoader<JanusCredential>
{
	
	// CONSTANT METHODS
	@Override
	protected String OBJECT_NAME()
	{
		return "credential";
	}
	
	
	// LOADER
	@Override
	protected JanusCredential deserialize(String configContent)
	{
		return GsonUtil.get().fromJson(configContent, JanusCredential.class);
	}
	
	@Override
	protected Map<Class<?>,Object> getDependenciesToInject()
	{
		return new HashMap<>();
	}
	
}
