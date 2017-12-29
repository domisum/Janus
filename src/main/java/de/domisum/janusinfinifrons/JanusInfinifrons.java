package de.domisum.janusinfinifrons;

import de.domisum.janusinfinifrons.component.ComponentSerializer;
import de.domisum.janusinfinifrons.component.CredentialComponent;
import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.janusinfinifrons.credential.Credential;
import de.domisum.janusinfinifrons.credential.CredentialSerializer;
import de.domisum.janusinfinifrons.storage.StringOnDiskStorage;
import de.domisum.janusinfinifrons.storage.StringSerializedObjectStorage;
import de.domisum.lib.auxilium.contracts.source.FiniteSource;
import de.domisum.lib.auxilium.contracts.storage.InMemoryProxyStorage;
import de.domisum.lib.auxilium.util.PHR;
import de.domisum.lib.auxilium.util.java.ThreadUtil;
import de.domisum.lib.auxilium.util.java.exceptions.InvalidConfigurationException;

import java.io.File;
import java.util.Optional;

public final class JanusInfinifrons
{

	// CONSTANTS
	private static final File COMPONENT_BASE_DIRECTORY = new File("components/");

	// STORAGE
	private FiniteSource<String, Credential> credentialStorage;
	private FiniteSource<String, JanusComponent> componentStorage;

	// REFERENCES
	private UpdateTicker ticker;


	// INIT
	public static void main(String[] args)
	{
		ThreadUtil.logUncaughtExceptions(Thread.currentThread());
		new JanusInfinifrons();
	}

	public void shutdown()
	{
		onShutdown();
	}

	private JanusInfinifrons()
	{
		ThreadUtil.registerShutdownHook(this::onShutdown);

		initSources();
		validateSettings();
		initTicker();
	}

	private void onShutdown()
	{
		ticker.stop();
	}


	// STORAGE
	private void initSources()
	{
		// @formatter:off
		InMemoryProxyStorage<String, Credential> credentialStorage = new InMemoryProxyStorage<>(
				new StringSerializedObjectStorage<>(
						new CredentialSerializer(),
						new StringOnDiskStorage(new File("config/credentials"), "jns_cred.json")
				)
		);
		credentialStorage.fetchAllToMemory();
		this.credentialStorage = credentialStorage;

		InMemoryProxyStorage<String, JanusComponent> componentStorage = new InMemoryProxyStorage<>(
				new StringSerializedObjectStorage<>(
						new ComponentSerializer(),
						new StringOnDiskStorage(new File("config/components"), "jns_comp.json")
				)
		);
		componentStorage.fetchAllToMemory();
		this.componentStorage = componentStorage;
		// @formatter:on
	}

	private void validateSettings()
	{
		credentialStorage.fetchAll().forEach(Credential::validate);
		validateComponents();
	}

	private void validateComponents()
	{
		componentStorage.fetchAll().forEach(JanusComponent::validate);

		for(JanusComponent janusComponent : componentStorage.fetchAll())
			validateComponenent(janusComponent);
	}

	private void validateComponenent(JanusComponent component)
	{
		component.setHelperDirectory(new File(COMPONENT_BASE_DIRECTORY, component.getId()));

		if(component instanceof CredentialComponent)
		{
			CredentialComponent credentialComponent = (CredentialComponent) component;
			Optional<Credential> credentialOptional = credentialStorage.fetch(credentialComponent.getCredentialId());
			if(!credentialOptional.isPresent())
				throw new InvalidConfigurationException(PHR.r(
						"unknown credential id '{}' in component '{}'",
						credentialComponent.getCredentialId(),
						component.getId()));

			credentialComponent.injectCredential(credentialOptional.get());
		}
	}


	// TICKER
	private void initTicker()
	{
		ticker = new UpdateTicker(componentStorage);
	}

}
