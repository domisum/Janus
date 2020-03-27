package io.domisum.janus.build;

import io.domisum.janus.config.Configuration;
import io.domisum.janus.config.object.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProjectBuilder
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// BUILD
	public void build(Project project, Configuration configuration)
	{
		logger.info("Building project '{}'...", project.getId());
		
		// TODO
		
		logger.info("...Building project '{}' done", project.getId());
	}
	
}
