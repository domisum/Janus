package io.domisum.janus.component;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import io.domisum.lib.auxiliumlib.exceptions.ShouldNeverHappenError;
import io.domisum.lib.auxiliumlib.util.json.GsonUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class JanusComponentDeserializer
{
	
	// DEPENDENCIES
	private final Set<Binding> bindings;
	private final JanusComponentDependencies janusComponentDependencies;
	
	
	// DESERIALIZATION
	public JanusComponent deserialize(String json)
	{
		var jsonTree = GsonUtil.get().toJsonTree(json).getAsJsonObject();
		
		var componentClass = determineComponentClass(jsonTree);
		var janusComponent = GsonUtil.get().fromJson(jsonTree, componentClass);
		injectDependencies(janusComponent);
		
		return janusComponent;
	}
	
	private Class<? extends JanusComponent> determineComponentClass(JsonObject jsonTree)
	{
		String typeKey = jsonTree.get("type").getAsString();
		
		if(typeKey == null)
			throw new InvalidConfigurationException("component config file does not specify type");
		var componentClass = getComponentClass(typeKey);
		if(componentClass == null)
			throw new InvalidConfigurationException("no component type bound for type '"+typeKey+"'");
		
		return componentClass;
	}
	
	private void injectDependencies(JanusComponent janusComponent)
	{
		try
		{
			injectDependenciesUncaught(janusComponent);
		}
		catch(IllegalAccessException e)
		{
			throw new ShouldNeverHappenError(e);
		}
	}
	
	private void injectDependenciesUncaught(JanusComponent janusComponent)
			throws IllegalAccessException
	{
		var fields = new HashSet<Field>();
		Class<?> clazz = janusComponent.getClass();
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
				field.set(janusComponent, janusComponentDependencies);
		}
	}
	
	
	// BINDING
	private Class<? extends JanusComponent> getComponentClass(String typeId)
	{
		for(var binding : bindings)
			if(binding.getTypeKey().equals(typeId))
				return binding.getComponentClass();
		
		return null;
	}
	
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
