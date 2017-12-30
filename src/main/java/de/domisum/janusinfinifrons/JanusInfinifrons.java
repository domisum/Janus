package de.domisum.janusinfinifrons;

import de.domisum.janusinfinifrons.build.ProjectBuild;
import de.domisum.janusinfinifrons.build.ProjectBuilder;
import de.domisum.janusinfinifrons.component.ComponentSerializer;
import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.janusinfinifrons.credential.Credential;
import de.domisum.janusinfinifrons.instance.JanusProjectInstance;
import de.domisum.janusinfinifrons.intercom.IntercomServer;
import de.domisum.janusinfinifrons.intercom.IntercomServerInteractionFacade;
import de.domisum.janusinfinifrons.intercom.undertow.UndertowIntercomServer;
import de.domisum.janusinfinifrons.project.JanusProject;
import de.domisum.janusinfinifrons.storage.SerializedIdentifyableStorage;
import de.domisum.lib.auxilium.contracts.Identifyable;
import de.domisum.lib.auxilium.contracts.serialization.BasicToStringSerializer;
import de.domisum.lib.auxilium.contracts.source.FiniteSource;
import de.domisum.lib.auxilium.contracts.storage.InMemoryProxyStorage;
import de.domisum.lib.auxilium.contracts.storage.InMemoryStorage;
import de.domisum.lib.auxilium.contracts.storage.Storage;
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

	private static final int INTERCOM_SERVER_PORT = 8381;
	private final Logger logger = LoggerFactory.getLogger(getClass());


	// CONSTANTS
	private static final File COMPONENT_BASE_DIRECTORY = new File("components/");
	private static final File BUILDS_BASE_DIRECTORY = new File("builds/");

	// STORAGE
	private FiniteSource<String, Credential> credentialSource;
	private FiniteSource<String, JanusComponent> componentSource;
	private FiniteSource<String, JanusProject> projectSource;
	private FiniteSource<String, JanusProjectInstance> projectInstanceSource;

	private final Storage<JanusProject, ProjectBuild> latestBuilds = new InMemoryStorage<>();

	// REFERENCES
	private UpdateTicker ticker;
	private IntercomServer intercomServer;


	// INIT
	public static void main(String[] args)
	{
		ThreadUtil.logUncaughtExceptions(Thread.currentThread());
		new JanusInfinifrons();
	}

	private JanusInfinifrons()
	{
		ThreadUtil.registerShutdownHook(this::onShutdown);

		initConfigSources();
		initConfigObjects();

		initIntercomServer();
		initTicker();
		logger.info("Startup complete\n");
	}

	public void shutdown()
	{
		onShutdown();
	}

	private void onShutdown()
	{
		ticker.stop();
		intercomServer.stop();
	}


	// STORAGE
	private void initConfigSources()
	{
		// @formatter:off
		InMemoryProxyStorage<String, Credential> credentialStorage = new InMemoryProxyStorage<>(
				new SerializedIdentifyableStorage<>(
						new File("config/credentials"), ".jns_cred.json",
						new BasicToStringSerializer<>(Credential.class)
				)
		);
		credentialStorage.fetchAllToMemory();
		credentialSource = credentialStorage;

		InMemoryProxyStorage<String, JanusComponent> componentStorage = new InMemoryProxyStorage<>(
				new SerializedIdentifyableStorage<>(
						new File("config/components"), ".jns_comp.json",
						new ComponentSerializer()
				)
		);
		componentStorage.fetchAllToMemory();
		componentSource = componentStorage;

		InMemoryProxyStorage<String, JanusProject> projectStorage = new InMemoryProxyStorage<>(
				new SerializedIdentifyableStorage<>(
						new File("config/projects"), ".jns_proj.json",
						new BasicToStringSerializer<>(JanusProject.class)
				)
		);
		projectStorage.fetchAllToMemory();
		projectSource = projectStorage;

		InMemoryProxyStorage<String, JanusProjectInstance> projectInstanceStorage = new InMemoryProxyStorage<>(
				new SerializedIdentifyableStorage<>(
						new File("config/instances"), ".jns_inst.json",
						new BasicToStringSerializer<>(JanusProjectInstance.class)
				)
		);
		projectInstanceStorage.fetchAllToMemory();
		projectInstanceSource = projectInstanceStorage;
		// @formatter:on
	}

	private void initConfigObjects()
	{
		initCredentials();
		initComponents();
		initProjects();
		initProjectInstances();
	}

	private void initCredentials()
	{
		credentialSource.fetchAll().forEach(Credential::validate);
		logger.info("Loaded {} credential(s): {}",
				credentialSource.fetchAll().size(),
				Identifyable.getIdList(credentialSource.fetchAll()));
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
		logger.info("Injected credential '{}' into component '{}'", credentialOptional.get().getId(), component.getId());
	}


	private void initProjects()
	{
		projectSource.fetchAll().forEach(p->p.validate(componentSource));
		logger.info("Loaded {} project(s): {}",
				projectSource.fetchAll().size(),
				Identifyable.getIdList(projectSource.fetchAll()));
	}

	private void initProjectInstances()
	{
		projectInstanceSource.fetchAll().forEach(p->p.validate(projectSource));
		logger.info("Loaded {} project instance(s): {}",
				projectInstanceSource.fetchAll().size(),
				Identifyable.getIdList(projectInstanceSource.fetchAll()));
	}


	// OTHER COMPONENT INIT
	private void initTicker()
	{
		logger.info("Initiating ticker...");

		// @formatter:off
		ticker = new UpdateTicker(
				componentSource,
				projectSource,
				projectInstanceSource, new ProjectBuilder(BUILDS_BASE_DIRECTORY, componentSource),
		latestBuilds);
		// @formatter:on
	}

	private void initIntercomServer()
	{
		logger.info("Initiating intercom server...");

		// @formatter:off
		IntercomServerInteractionFacade interactionFacade = this::getLatestBuild;

		intercomServer = new UndertowIntercomServer(interactionFacade, INTERCOM_SERVER_PORT);
		// @formatter:on

		intercomServer.start();
	}


	// UTIL
	private ProjectBuild getLatestBuild(String projectName)
	{
		Optional<JanusProject> projectOptional = projectSource.fetch(projectName);
		if(!projectOptional.isPresent())
			throw new IllegalArgumentException("unknown project: "+projectName);

		Optional<ProjectBuild> latestBuild = latestBuilds.fetch(projectOptional.get());
		if(!latestBuild.isPresent())
			throw new IllegalArgumentException("no build for project "+projectName);

		return latestBuild.get();
	}

}
