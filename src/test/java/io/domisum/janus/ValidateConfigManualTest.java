package io.domisum.janus;

import io.domisum.janus.config.ConfigurationLoader;
import io.domisum.janus.dependencyinjection.JanusInjector;
import io.domisum.lib.auxiliumlib.config.ConfigException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidateConfigManualTest
{
	
	public static void main(String[] args)
		throws ConfigException
	{
		var configDir = new File(System.getProperty("user.home")+"/");
		var configLoader = JanusInjector.create().getInstance(ConfigurationLoader.class);
		configLoader.load(configDir);
	}
	
}
