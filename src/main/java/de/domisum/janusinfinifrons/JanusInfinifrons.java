package de.domisum.janusinfinifrons;

import de.domisum.janusinfinifrons.component.ComponentSerializer;
import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.janusinfinifrons.credential.Credential;
import de.domisum.janusinfinifrons.credential.CredentialSerializer;
import de.domisum.janusinfinifrons.storage.SerializedIdentifyableStorage;
import de.domisum.lib.auxilium.contracts.Identifyable;
import de.domisum.lib.auxilium.contracts.source.FiniteSource;
import de.domisum.lib.auxilium.contracts.storage.InMemoryProxyStorage;
import de.domisum.lib.auxilium.util.PHR;
import de.domisum.lib.auxilium.util.java.ThreadUtil;
import de.domisum.lib.auxilium.util.java.exceptions.InvalidConfigurationException;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

public final class JanusInfinifrons
{

	private final Logger logger = LoggerFactory.getLogger(getClass());


	// CONSTANTS
	private static final File COMPONENT_BASE_DIRECTORY = new File("components/");

	// STORAGE
	private FiniteSource<String, Credential> credentialSource;
	private FiniteSource<String, JanusComponent> componentSource;

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
		initConfigObjects();
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
				new SerializedIdentifyableStorage<>(
						new File("config/credentials"), "jns_cred.json",
						new CredentialSerializer()
				)
		);
		credentialStorage.fetchAllToMemory();
		credentialSource = credentialStorage;

		InMemoryProxyStorage<String, JanusComponent> componentStorage = new InMemoryProxyStorage<>(
				new SerializedIdentifyableStorage<>(
						new File("config/components"), "jns_comp.json",
						new ComponentSerializer()
				)
		);
		componentStorage.fetchAllToMemory();
		componentSource = componentStorage;
		// @formatter:on
	}

	private void initConfigObjects()
	{
		credentialSource.fetchAll().forEach(Credential::validate);
		logger.info("Loaded {} credential(s): {}",
				credentialSource.fetchAll().size(),
				Identifyable.getIdList(credentialSource.fetchAll()));

		initComponents();
	}

	private void initComponents()
	{
		componentSource.fetchAll().forEach(JanusComponent::validate);
		logger.info("Loaded {} component(s): {}",
				componentSource.fetchAll().size(),
				Identifyable.getIdList(componentSource.fetchAll()));

		for(JanusComponent janusComponent : componentSource.fetchAll())
			initComponenent(janusComponent);
	}

	private void initComponenent(JanusComponent component)
	{
		Validate.notNull(component.getId(), "component id was null: "+component);

		component.setHelperDirectory(new File(COMPONENT_BASE_DIRECTORY, component.getId()));
		injectComponentCredential(component);
	}

	private void injectComponentCredential(JanusComponent component)
	{
		if(component.getCredentialId() == null)
		{
			logger.info("Did not inject credential into componenent: {}", component.getId());
			return;
		}

		Optional<Credential> credentialOptional = credentialSource.fetch(component.getCredentialId());
		if(!credentialOptional.isPresent())
			throw new InvalidConfigurationException(PHR.r("unknown credential id '{}' in component '{}'",
					component.getCredentialId(),
					component.getId()));

		component.injectCredential(credentialOptional.get());
		logger.info("Injected credential '{}' into component '{}'", credentialOptional.get(), component.getId());
	}


	// TICKER
	private void initTicker()
	{
		ticker = new UpdateTicker(componentSource);
	}

}
