package io.domisum.janus.config.object.credentials;

import io.domisum.janus.config.object.ConfigObjectLoader;
import io.domisum.lib.auxiliumlib.util.GsonUtil;

import java.util.HashMap;
import java.util.Map;

public class CredentialLoader
		extends ConfigObjectLoader<Credential>
{
	
	// CONSTANT METHODS
	@Override
	protected String OBJECT_NAME()
	{
		return "credential";
	}
	
	
	// LOADER
	@Override
	protected Credential deserialize(String configContent)
	{
		return GsonUtil.get().fromJson(configContent, Credential.class);
	}
	
	@Override
	protected Map<Class<?>,Object> getDependenciesToInject()
	{
		return new HashMap<>();
	}
	
}
