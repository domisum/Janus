package de.domisum.janusinfinifrons;

import de.domisum.janusinfinifrons.component.ComponentSerializer;
import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.janusinfinifrons.credential.Credential;
import de.domisum.janusinfinifrons.credential.CredentialSerializer;
import de.domisum.janusinfinifrons.storage.OnDiskSettings;
import de.domisum.janusinfinifrons.storage.StringOnDiskStorage;
import de.domisum.janusinfinifrons.storage.StringSerializedObjectStorage;
import de.domisum.lib.auxilium.contracts.storage.InMemoryProxyStorage;

import java.io.File;

public final class JanusInfinifrons
{

	// CONSTANTS
	private static final OnDiskSettings CREDENTIALS_ON_DISK_SETTINGS = new OnDiskSettings(new File("credentials"), "jns_cred");

	private static final OnDiskSettings COMPONENTS_ON_DISK_SETTINGS = new OnDiskSettings(new File("components"), "jns_comp");

	// STORAGE
	private InMemoryProxyStorage<String, Credential> credentialStorage;
	private InMemoryProxyStorage<String, JanusComponent> componentStorage;


	// INIT
	public static void main(String[] args)
	{
		new JanusInfinifrons();
	}

	private JanusInfinifrons()
	{
		initStorage();
		loadSettings();
	}


	// STORAGE
	private void initStorage()
	{
		credentialStorage = new InMemoryProxyStorage<>(new StringSerializedObjectStorage<>(new CredentialSerializer(),
				new StringOnDiskStorage(CREDENTIALS_ON_DISK_SETTINGS)));

		componentStorage = new InMemoryProxyStorage<>(new StringSerializedObjectStorage<>(new ComponentSerializer(),
				new StringOnDiskStorage(COMPONENTS_ON_DISK_SETTINGS)));
	}

	private void loadSettings()
	{
		credentialStorage.fetchAllToMemory();
		componentStorage.fetchAllToMemory();
	}

}
