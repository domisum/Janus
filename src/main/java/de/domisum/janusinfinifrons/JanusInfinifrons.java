package de.domisum.janusinfinifrons;

import de.domisum.janusinfinifrons.component.ComponentSerializer;
import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.janusinfinifrons.credential.Credential;
import de.domisum.janusinfinifrons.credential.CredentialSerializer;
import de.domisum.janusinfinifrons.storage.InMemoryCopyStorage;
import de.domisum.janusinfinifrons.storage.ondisk.ObjectOnDiskStorage;

import java.io.File;

public class JanusInfinifrons
{

	// CONSTANTS
	private static final File CREDENTIALS_DIRECTORY = new File("credentials");
	private static final String CREDENTIALS_FILE_EXTENSION = "jns_cred";

	private static final File COMPONENTS_DIRECTORY = new File("components");
	private static final String COMPONENTS_FILE_EXTENSION = "jns_comp";

	// STORAGE
	private InMemoryCopyStorage<Credential> credentialStorage;
	private InMemoryCopyStorage<JanusComponent> componentStorage;


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
		credentialStorage = new InMemoryCopyStorage<>(
				new ObjectOnDiskStorage<>(new CredentialSerializer(), CREDENTIALS_DIRECTORY, CREDENTIALS_FILE_EXTENSION));

		componentStorage = new InMemoryCopyStorage<>(
				new ObjectOnDiskStorage<>(new ComponentSerializer(), COMPONENTS_DIRECTORY, COMPONENTS_FILE_EXTENSION));
	}

	private void loadSettings()
	{
		credentialStorage.loadFromSource();
		componentStorage.loadFromSource();
	}

}
