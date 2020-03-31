package io.domisum.janus._manualtest;

import io.domisum.janus.config.ConfigurationLoader;
import io.domisum.janus.dependencyinjection.JanusInjector;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidateConfig
{
	
	public static void main(String[] args)
			throws InvalidConfigurationException
	{
		var configDir = new File("C:\\Users\\domisum\\testChamber\\domisumReplay\\repos\\___dR-JanusConfig-VideoCreator");
		
		var configLoader = JanusInjector.create().getInstance(ConfigurationLoader.class);
		configLoader.load(configDir);
	}
	
}
