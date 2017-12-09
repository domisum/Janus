package de.domisum.janusinfinifrons.credential;

import de.domisum.janusinfinifrons.storage.ToStringSerializer;
import de.domisum.lib.auxilium.util.json.GsonUtil;

public class CredentialSerializer implements ToStringSerializer<Credential>
{

	// SERIALIZE
	@Override public String serialize(Credential object)
	{
		return GsonUtil.get().toJson(object);
	}

	@Override public Credential deserialize(String objectString)
	{
		return GsonUtil.get().fromJson(objectString, Credential.class);
	}

}
