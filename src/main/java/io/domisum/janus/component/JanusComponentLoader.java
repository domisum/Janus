package io.domisum.janus.component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import io.domisum.janus.JanusConfigObjectLoader;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import io.domisum.lib.auxiliumlib.util.json.GsonUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class JanusComponentLoader
		extends JanusConfigObjectLoader<JanusComponent>
{
	
	// DEPENDENCIES
	private final Set<Binding> bindings;
	private final JanusComponentDependencies janusComponentDependencies;
	
	
	// CONSTANT METHODS
	@Override
	protected String OBJECT_NAME()
	{
		return "component";
	}
	
	
	// DESERIALIZATION
	@Override
	protected JanusComponent deserialize(String json)
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
	@Override
	protected Map<Class<?>,Object> getDependenciesToInject()
	{
		return Map.of(JanusComponentDependencies.class, janusComponentDependencies);
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
