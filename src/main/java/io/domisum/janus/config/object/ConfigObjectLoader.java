package io.domisum.janus.config.object;

import com.google.gson.JsonParseException;
import io.domisum.janus.Janus;
import io.domisum.lib.auxiliumlib.PHR;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import io.domisum.lib.auxiliumlib.exceptions.ShouldNeverHappenError;
import io.domisum.lib.auxiliumlib.util.StringUtil;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import io.domisum.lib.auxiliumlib.util.file.FileUtil.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
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
	
	private File CONFIG_SUB_DIRECTORY()
	{
		return new File(Janus.CONFIG_DIRECTORY, OBJECT_NAME_PLURAL());
	}
	
	private String FILE_EXTENSION()
	{
		return OBJECT_NAME()+".json";
	}
	
	
	// LOADING
	public ConfigObjectRegistry<T> load()
			throws InvalidConfigurationException
	{
		logger.info("Loading {}...", OBJECT_NAME_PLURAL());
		
		var files = FileUtil.listFilesRecursively(CONFIG_SUB_DIRECTORY(), FileType.FILE);
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
		injectDependencies(configObject);
		var validationReport = configObject.validate();
		
		String configObjectIdFromFileName = FileUtil.getNameWithoutCompositeExtension(file);
		if(!Objects.equals(configObject.getId(), configObjectIdFromFileName))
		{
			String exceptionMessage = PHR.r("{} id '{}' does not match file name: '{}'",
					OBJECT_NAME(), configObject.getId(), file.getName());
			throw new InvalidConfigurationException(exceptionMessage);
		}
		
		if(validationReport != null)
			logger.info("Validated {} '{}': {}", OBJECT_NAME(), configObject.getId(), validationReport);
		logger.info("Loaded {} {}", OBJECT_NAME(), configObject);
		
		return configObject;
	}
	
	protected abstract T deserialize(String configContent)
			throws InvalidConfigurationException;
	
	protected abstract Map<Class<?>,Object> getDependenciesToInject();
	
	private void injectDependencies(T component)
	{
		try
		{
			injectDependenciesUncaught(component);
		}
		catch(IllegalAccessException e)
		{
			throw new ShouldNeverHappenError(e);
		}
	}
	
	private void injectDependenciesUncaught(T component)
			throws IllegalAccessException
	{
		var dependenciesToInject = getDependenciesToInject();
		
		var fields = getAllFields(component);
		for(var field : fields)
		{
			field.setAccessible(true);
			for(var dependency : dependenciesToInject.entrySet())
				if(field.getType() == dependency.getKey())
					field.set(component, dependency.getValue());
		}
	}
	
	private HashSet<Field> getAllFields(T component)
	{
		var fields = new HashSet<Field>();
		Class<?> clazz = component.getClass();
		do
		{
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		}
		while(clazz != Object.class);
		return fields;
	}
	
}
