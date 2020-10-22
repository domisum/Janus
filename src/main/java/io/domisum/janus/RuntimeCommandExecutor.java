package io.domisum.janus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RuntimeCommandExecutor
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// RUN
	public void executeCommand(String command)
	{
		logger.info("Executing command '{}'", command);
		try
		{
			Runtime.getRuntime().exec(command);
		}
		catch(IOException e)
		{
			logger.error("Failed to execute command '{}'", command, e);
		}
	}
	
}
