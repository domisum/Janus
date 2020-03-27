package io.domisum.janus.config.object;

public interface ConfigObject
{
	
	String getId();
	
	ValidationReport validate();
	
}
