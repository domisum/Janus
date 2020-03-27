package io.domisum.janus.config.object.component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import io.domisum.janus.config.object.ConfigObjectLoader;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import io.domisum.lib.auxiliumlib.util.json.GsonUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ComponentLoader
		extends ConfigObjectLoader<Component>
{
	
	// DEPENDENCIES
	private final Set<Binding> bindings;
	private final ComponentDependencies componentDependencies;
	
	
	// CONSTANT METHODS
	@Override
	protected String OBJECT_NAME()
	{
		return "component";
	}
	
	
	// DESERIALIZATION
	@Override
	protected Component deserialize(String json)
	{
		var jsonTree = JsonParser.parseString(json).getAsJsonObject();
		var componentClass = determineComponentClass(jsonTree);
		var janusComponent = GsonUtil.get().fromJson(jsonTree, componentClass);
		
		return janusComponent;
	}
	
	private Class<? extends Component> determineComponentClass(JsonObject jsonTree)
	{
		String typeKey = jsonTree.get("type").getAsString();
		
		if(typeKey == null)
			throw new InvalidConfigurationException("component config file does not specify type");
		var componentClass = getBoundComponentClass(typeKey);
		if(componentClass == null)
			throw new InvalidConfigurationException("no component type bound for type '"+typeKey+"'");
		
		return componentClass;
	}
	
	private Class<? extends Component> getBoundComponentClass(String typeId)
	{
		for(var binding : bindings)
			if(binding.getTypeKey().equals(typeId))
				return binding.getComponentClass();
		
		return null;
	}
	
	
	// DEPENDENCIES
	@Override
	protected Map<Class<?>,Object> getDependenciesToInject()
	{
		return Map.of(ComponentDependencies.class, componentDependencies);
	}
	
	
	// BINDING
	@RequiredArgsConstructor
	@EqualsAndHashCode(of = "typeKey")
	public static class Binding
	{
		
		@Getter
		private final String typeKey;
		@Getter
		private final Class<? extends Component> componentClass;
		
	}
	
}
