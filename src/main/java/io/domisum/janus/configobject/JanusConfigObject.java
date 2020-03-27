package io.domisum.janus.configobject;

import io.domisum.janus.ValidationReport;

public interface JanusConfigObject
{
	
	String getId();
	
	ValidationReport validate();
	
}
