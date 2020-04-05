package io.domisum.janus.config.object;

import io.domisum.lib.auxiliumlib.config.ConfigObject;
import io.domisum.lib.auxiliumlib.config.ConfigObjectLoader;

public abstract class JanusConfigObjectLoader<T extends ConfigObject> extends ConfigObjectLoader<T>
{
	
	// CONSTANT METHODS
	@Override
	protected String FILE_EXTENSION()
	{
		return OBJECT_NAME()+".json";
	}
	
}
