package io.domisum.janus.configobject;

public interface JanusConfigObject
{
	
	String getId();
	
	ValidationReport validate();
	
}
