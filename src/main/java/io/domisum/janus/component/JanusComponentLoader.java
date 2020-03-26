package io.domisum.janus.component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import io.domisum.janus.Janus;
import io.domisum.lib.auxiliumlib.PHR;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import io.domisum.lib.auxiliumlib.exceptions.ShouldNeverHappenError;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import io.domisum.lib.auxiliumlib.util.file.FileUtil.FileType;
import io.domisum.lib.auxiliumlib.util.json.GsonUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class JanusComponentLoader
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// CONSTANTS
	private static final File CONFIG_COMPONENT_DIRECTORY = new File(Janus.CONFIG_DIRECTORY, "components");
	private static final String FILE_EXTENSION = "component.json";
	
	// DEPENDENCIES
	private final Set<Binding> bindings;
	private final JanusComponentDependencies janusComponentDependencies;
	
	
	// LOADING
	public Set<JanusComponent> load()
			throws InvalidConfigurationException
	{
		var files = FileUtil.listFilesRecursively(CONFIG_COMPONENT_DIRECTORY, FileType.FILE);
		var components = new HashSet<JanusComponent>();
		for(var file : files)
			if(FILE_EXTENSION.equals(FileUtil.getCompositeExtension(file)))
				components.add(loadComponentFromFile(file));
			else
				logger.warn("Config dir of components contains file with wrong extension: '{}' (expected extension: '{}')",
						file.getName(), FILE_EXTENSION);
		
		return components;
	}
	
	private JanusComponent loadComponentFromFile(File file)
			throws InvalidConfigurationException
	{
		String fileContent = FileUtil.readString(file);
		var component = deserialize(fileContent);
		injectDependencies(component);
		
		try
		{
			var validationReport = component.validate();
			
			String componentIdFromFileName = FileUtil.getNameWithoutCompositeExtension(file);
			if(!Objects.equals(component.getId(), componentIdFromFileName))
			{
				String exceptionMessage = PHR.r("component id ({}) does not match file name: {}", component.getId(), file.getName());
				throw new InvalidConfigurationException(exceptionMessage);
			}
			
			logger.info("Loaded component {}", component);
			logger.info("Validated component {}: {}", component.getId(), validationReport);
		}
		catch(RuntimeException e)
		{
			throw new InvalidConfigurationException("invalid configuration of component from file '"+file.getName()+"'", e);
		}
		
		return component;
	}
	
	
	// DESERIALIZATION
	private JanusComponent deserialize(String json)
	{
		var jsonTree = JsonParser.parseString(json).getAsJsonObject();
		
		var componentClass = determineComponentClass(jsonTree);
		var janusComponent = GsonUtil.get().fromJson(jsonTree, componentClass);
		
		return janusComponent;
	}
	
	private Class<? extends JanusComponent> determineComponentClass(JsonObject jsonTree)
	{
		String typeKey = jsonTree.get("type").getAsString();
		
		if(typeKey == null)
			throw new InvalidConfigurationException("component config file does not specify type");
		var componentClass = getBoundComponentClass(typeKey);
		if(componentClass == null)
			throw new InvalidConfigurationException("no component type bound for type '"+typeKey+"'");
		
		return componentClass;
	}
	
	private Class<? extends JanusComponent> getBoundComponentClass(String typeId)
	{
		for(var binding : bindings)
			if(binding.getTypeKey().equals(typeId))
				return binding.getComponentClass();
		
		return null;
	}
	
	
	// DEPENDENCIES
	private void injectDependencies(JanusComponent component)
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
	
	private void injectDependenciesUncaught(JanusComponent component)
			throws IllegalAccessException
	{
		var fields = new HashSet<Field>();
		Class<?> clazz = component.getClass();
		do
		{
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		}
		while(JanusComponent.class.isAssignableFrom(clazz));
		
		for(var field : fields)
		{
			field.setAccessible(true);
			if(field.getType() == JanusComponentDependencies.class)
				field.set(component, janusComponentDependencies);
		}
	}
	
	
	// BINDING
	@RequiredArgsConstructor
	@EqualsAndHashCode(of = "typeKey")
	public static class Binding
	{
		
		@Getter
		private final String typeKey;
		@Getter
		private final Class<? extends JanusComponent> componentClass;
		
	}
	
}
