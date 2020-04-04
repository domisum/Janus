package io.domisum.janus.config.object;

import com.google.gson.JsonParseException;
import io.domisum.lib.auxiliumlib.PHR;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import io.domisum.lib.auxiliumlib.util.StringUtil;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import io.domisum.lib.auxiliumlib.util.file.FileUtil.FileType;
import io.domisum.lib.auxiliumlib.util.java.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ConfigObjectLoader<T extends ConfigObject>
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// CONSTANT METHODS
	protected abstract String OBJECT_NAME();
	
	private String OBJECT_NAME_PLURAL()
	{
		return OBJECT_NAME()+"s";
	}
	
	private String CONFIG_SUB_DIRECTORY_NAME()
	{
		return OBJECT_NAME_PLURAL();
	}
	
	private String FILE_EXTENSION()
	{
		return OBJECT_NAME()+".json";
	}
	
	
	// LOADING
	public ConfigObjectRegistry<T> load(File configDirectory)
			throws InvalidConfigurationException
	{
		logger.info("Loading {}...", OBJECT_NAME_PLURAL());
		
		var configSubDirectory = new File(configDirectory, CONFIG_SUB_DIRECTORY_NAME());
		var files = FileUtil.listFilesRecursively(configSubDirectory, FileType.FILE);
		var configObjects = new HashSet<T>();
		for(var file : files)
			if(FILE_EXTENSION().equals(FileUtil.getCompositeExtension(file)))
				configObjects.add(loadConfigObjectFromFile(file));
			else
				logger.warn("Config subdir of {} contains file with wrong extension: '{}' (expected extension: '{}')",
						OBJECT_NAME_PLURAL(), file.getName(), FILE_EXTENSION());
		
		if(configObjects.isEmpty())
			logger.info("(there are no {})", OBJECT_NAME_PLURAL());
		
		var configObjectIds = configObjects.stream().map(ConfigObject::getId).collect(Collectors.toSet());
		logger.info("...Loading {} done, loaded {}: [{}]", OBJECT_NAME_PLURAL(), configObjects.size(), StringUtil.collectionToString(configObjectIds, ", "));
		return new ConfigObjectRegistry<>(configObjects);
	}
	
	private T loadConfigObjectFromFile(File file)
			throws InvalidConfigurationException
	{
		String fileContent = FileUtil.readString(file);
		try
		{
			return createConfigObject(file, fileContent);
		}
		catch(JsonParseException|InvalidConfigurationException e)
		{
			String message = PHR.r("invalid configuration of {} from file '{}'", OBJECT_NAME(), file.getName());
			throw new InvalidConfigurationException(message, e);
		}
	}
	
	private T createConfigObject(File file, String fileContent)
			throws InvalidConfigurationException
	{
		var configObject = deserialize(fileContent);
		
		String id = FileUtil.getNameWithoutCompositeExtension(file);
		ReflectionUtil.injectValue(configObject, "id", id);
		for(var dependency : getDependenciesToInject().entrySet())
			ReflectionUtil.injectValue(configObject, dependency.getKey(), dependency.getValue());
		
		configObject.validate();
		
		logger.info("Loaded {} {}", OBJECT_NAME(), configObject);
		return configObject;
	}
	
	
	// ABSTRACT
	protected abstract T deserialize(String configContent)
			throws InvalidConfigurationException;
	
	protected abstract Map<Class<?>,Object> getDependenciesToInject();
	
}
