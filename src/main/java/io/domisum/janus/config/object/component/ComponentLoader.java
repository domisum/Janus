package io.domisum.janus.config.object.component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import io.domisum.janus.config.object.JanusConfigObjectLoader;
import io.domisum.lib.auxiliumlib.config.ConfigException;
import io.domisum.lib.auxiliumlib.util.GsonUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ComponentLoader
	extends JanusConfigObjectLoader<Component>
{
	
	// DEPENDENCIES
	private final Set<Binding> bindings;
	private final ComponentDependencyFacade componentDependencyFacade;
	
	
	// CONSTANT METHODS
	@Override
	protected String OBJECT_NAME()
	{
		return "component";
	}
	
	
	// DESERIALIZATION
	@Override
	protected Component deserialize(String json)
		throws ConfigException
	{
		var jsonTree = JsonParser.parseString(json).getAsJsonObject();
		var componentClass = determineComponentClass(jsonTree);
		return GsonUtil.get().fromJson(jsonTree, componentClass);
	}
	
	private Class<? extends Component> determineComponentClass(JsonObject jsonTree)
		throws ConfigException
	{
		var typeJsonElement = jsonTree.get("type");
		if(typeJsonElement == null)
			throw new ConfigException("No component type set");
		String typeKey = typeJsonElement.getAsString();
		
		if(typeKey == null)
			throw new ConfigException("Component config file does not specify type");
		var componentClassOptional = getBoundComponentClass(typeKey);
		if(componentClassOptional.isEmpty())
			throw new ConfigException("No component type bound for type '"+typeKey+"'");
		
		return componentClassOptional.get();
	}
	
	private Optional<Class<? extends Component>> getBoundComponentClass(String typeId)
	{
		for(var binding : bindings)
			if(binding.getTypeKey().equals(typeId))
				return Optional.of(binding.getComponentClass());
		
		return Optional.empty();
	}
	
	
	// DEPENDENCIES
	@Override
	protected Map<Class<?>, Object> getDependenciesToInject()
	{
		return Map.of(ComponentDependencyFacade.class, componentDependencyFacade);
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
