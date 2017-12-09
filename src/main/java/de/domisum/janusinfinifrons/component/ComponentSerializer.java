package de.domisum.janusinfinifrons.component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.domisum.janusinfinifrons.component.components.GitRepositoryComponent;
import de.domisum.janusinfinifrons.component.components.NexusArtifactComponent;
import de.domisum.janusinfinifrons.storage.ToStringSerializer;
import de.domisum.lib.auxilium.util.json.RuntimeTypeAdapterFactory;

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
	public String serialize(JanusComponent component)
	{
		return gsonWithTypeAdapterFactory.toJson(component);
	}

	public JanusComponent deserialize(String componentString)
	{
		return gsonWithTypeAdapterFactory.fromJson(componentString, JanusComponent.class);
	}

}
