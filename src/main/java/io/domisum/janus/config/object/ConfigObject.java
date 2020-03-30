package io.domisum.janus.config.object;

import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;

public interface ConfigObject
{
	
	String getId();
	
	void validate()
			throws InvalidConfigurationException;
	
}
