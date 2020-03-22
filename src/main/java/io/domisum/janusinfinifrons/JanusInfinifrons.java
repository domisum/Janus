package io.domisum.janusinfinifrons;

import io.domisum.janusinfinifrons.build.ProjectBuild;
import io.domisum.janusinfinifrons.build.ProjectBuilder;
import io.domisum.janusinfinifrons.component.ComponentSerializer;
import io.domisum.janusinfinifrons.component.JanusComponent;
import io.domisum.janusinfinifrons.credential.Credential;
import io.domisum.janusinfinifrons.instance.JanusProjectInstance;
import io.domisum.janusinfinifrons.intercom.IntercomServer;
import io.domisum.janusinfinifrons.intercom.IntercomServerInteractionFacade;
import io.domisum.janusinfinifrons.project.JanusProject;
import io.domisum.lib.auxiliumlib.contracts.Identifyable;
import io.domisum.lib.auxiliumlib.contracts.serialization.GsonSerializer;
import io.domisum.lib.auxiliumlib.contracts.source.optional.FiniteOptionalSource;
import io.domisum.lib.auxiliumlib.contracts.storage.InMemoryProxyStorage;
import io.domisum.lib.auxiliumlib.contracts.storage.InMemoryStorage;
import io.domisum.lib.auxiliumlib.contracts.storage.SerializedIdentifyableStorage;
import io.domisum.lib.auxiliumlib.contracts.storage.Storage;
import io.domisum.lib.auxiliumlib.util.PHR;
import io.domisum.lib.auxiliumlib.util.java.ThreadUtil;
import io.domisum.lib.auxiliumlib.util.java.exceptions.InvalidConfigurationException;
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
	private FiniteOptionalSource<String, Credential> credentialSource;
	private FiniteOptionalSource<String, JanusComponent> componentSource;
	private FiniteOptionalSource<String, JanusProject> projectSource;
	private FiniteOptionalSource<String, JanusProjectInstance> projectInstanceSource;

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
		initConfigSources();
		initConfigObjects();

		initTicker();
		initIntercomServer();
		logger.info("Startup complete\n");
	}

	private void shutdown() // TODO maybe use later for self update
	{
		intercomServer.stop();
		ticker.stopSoft();
	}


	// STORAGE
	private void initConfigSources()
	{
		// @formatter:off
		InMemoryProxyStorage<String, Credential> credentialStorage = new InMemoryProxyStorage<>(
				new SerializedIdentifyableStorage<>(
						new File("config/credentials"), "jns_cred.json",
						new GsonSerializer<>(Credential.class)
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

		InMemoryProxyStorage<String, JanusProject> projectStorage = new InMemoryProxyStorage<>(
				new SerializedIdentifyableStorage<>(
						new File("config/projects"), "jns_proj.json",
						new GsonSerializer<>(JanusProject.class)
				)
		);
		projectStorage.fetchAllToMemory();
		projectSource = projectStorage;

		InMemoryProxyStorage<String, JanusProjectInstance> projectInstanceStorage = new InMemoryProxyStorage<>(
				new SerializedIdentifyableStorage<>(
						new File("config/instances"), "jns_inst.json",
						new GsonSerializer<>(JanusProjectInstance.class)
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
				Identifyable.getIdList(credentialSource.fetchAll())
		);
	}

	private void initComponents()
	{
		componentSource.fetchAll().forEach(JanusComponent::validate);
		logger.info("Loaded {} component(s): {}",
				componentSource.fetchAll().size(),
				Identifyable.getIdList(componentSource.fetchAll())
		);

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
		if(credentialOptional.isEmpty())
			throw new InvalidConfigurationException(PHR.r("unknown credential id '{}' in component '{}'",
					component.getCredentialId(),
					component.getId()
			));

		component.injectCredential(credentialOptional.get());
		logger.info("Injected credential '{}' into component '{}'", credentialOptional.get().getId(), component.getId());
	}


	private void initProjects()
	{
		projectSource.fetchAll().forEach(p->p.validate(componentSource));
		logger.info("Loaded {} project(s): {}",
				projectSource.fetchAll().size(),
				Identifyable.getIdList(projectSource.fetchAll())
		);
	}

	private void initProjectInstances()
	{
		projectInstanceSource.fetchAll().forEach(p->p.validate(projectSource));
		logger.info("Loaded {} project instance(s): {}",
				projectInstanceSource.fetchAll().size(),
				Identifyable.getIdList(projectInstanceSource.fetchAll())
		);
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

		IntercomServerInteractionFacade interactionFacade = this::getLatestBuild;
		intercomServer = new IntercomServer(INTERCOM_SERVER_PORT, interactionFacade);
		intercomServer.start();
	}


	// UTIL
	private ProjectBuild getLatestBuild(String projectName)
	{
		Optional<JanusProject> projectOptional = projectSource.fetch(projectName);
		if(projectOptional.isEmpty())
			throw new IllegalArgumentException("unknown project: "+projectName);

		Optional<ProjectBuild> latestBuild = latestBuilds.fetch(projectOptional.get());
		return latestBuild.orElse(null);
	}

}
