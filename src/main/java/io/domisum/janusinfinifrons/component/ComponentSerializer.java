package io.domisum.janusinfinifrons.component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.domisum.janusinfinifrons.component.components.GitRepositoryComponent;
import io.domisum.janusinfinifrons.component.components.NexusArtifactComponent;
import io.domisum.lib.auxiliumlib.contracts.serialization.ToStringSerializer;
import io.domisum.lib.auxiliumlib.util.json.RuntimeTypeAdapterFactory;

public class ComponentSerializer implements ToStringSerializer<JanusComponent>
{

	private Gson gsonWithTypeAdapterFactory;


	// INIT
	public ComponentSerializer()
	{
		RuntimeTypeAdapterFactory<JanusComponent> serializerFactory = RuntimeTypeAdapterFactory
				.of(JanusComponent.class, "componentType");

		serializerFactory.registerSubtype(GitRepositoryComponent.class, "GitRepository");
		serializerFactory.registerSubtype(NexusArtifactComponent.class, "NexusArtifact");

		gsonWithTypeAdapterFactory = new GsonBuilder().registerTypeAdapterFactory(serializerFactory).create();
	}


	// SERIALIZE
	@Override public String serialize(JanusComponent component)
	{
		return gsonWithTypeAdapterFactory.toJson(component);
	}

	@Override public JanusComponent deserialize(String componentString)
	{
		JanusComponent component = gsonWithTypeAdapterFactory.fromJson(componentString, JanusComponent.class);

		if(component == null)
			throw new RuntimeException("got empty string to deserialize");

		return component;
	}

}
