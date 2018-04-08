package de.domisum.janusinfinifrons.component.components;

import de.domisum.janusinfinifrons.build.ProjectBuild;
import de.domisum.janusinfinifrons.component.CredentialComponent;
import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.janusinfinifrons.project.ProjectComponentDependency;
import de.domisum.lib.auxilium.data.container.AbstractURL;
import de.domisum.lib.auxilium.util.FileUtil;
import de.domisum.lib.auxilium.util.PHR;
import de.domisum.lib.auxilium.util.http.HttpCredentials;
import de.domisum.lib.auxilium.util.http.HttpFetch;
import de.domisum.lib.auxilium.util.http.specific.HttpFetchString;
import de.domisum.lib.auxilium.util.http.specific.HttpFetchToFile;
import de.domisum.lib.auxilium.util.java.annotations.InitByDeserialization;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor
public class NexusArtifactComponent extends JanusComponent implements CredentialComponent
{

	private static final Logger logger = LoggerFactory.getLogger(NexusArtifactComponent.class);


	// SETTINGS
	@InitByDeserialization private String serverUrl;
	@InitByDeserialization private String repositoryName;

	@InitByDeserialization private String groupId;
	@InitByDeserialization private String artifactId;
	@InitByDeserialization private String version;

	// STATUS
	private transient String currentJarMd5 = null;


	// INIT
	@Override public void validate()
	{
		// nothing to validate yet
	}


	// COMPONENT
	@Override public String getVersion()
	{
		if(currentJarMd5 == null)
			throw new IllegalStateException("can't check version before first update");

		return currentJarMd5;
	}

	@Override public void update()
	{
		String lastJarMd5 = currentJarMd5;
		fetchJarMd5();

		boolean md5Changed = !Objects.equals(lastJarMd5, currentJarMd5);
		if(md5Changed)
			downloadJar();
	}

	@Override public void addToBuildThrough(ProjectComponentDependency projectComponentDependency, ProjectBuild build)
	{
		File targetDirectory = new File(build.getDirectory(), projectComponentDependency.getInBuildPath());
		File targetFile = new File(targetDirectory, getJarFile().getName());

		FileUtil.copyFile(getJarFile(), targetFile);
	}


	// FETCH
	private void fetchJarMd5()
	{
		AbstractURL url = getUrl("jar.md5");

		HttpFetch<String> fetchString = new HttpFetchString(url).onFail(e->logger.error("failed to fetch jar md5", e));
		if(getCredential() != null)
			fetchString.credentials(new HttpCredentials(getCredential().getUsername(), getCredential().getPassword()));

		Optional<String> jarMd5Optional = fetchString.fetch();
		logger.debug("jarMd5: {}", jarMd5Optional);
		jarMd5Optional.ifPresent(s->currentJarMd5 = s);
	}

	private void downloadJar()
	{
		AbstractURL url = getUrl("jar");
		HttpFetch<File> httpFetchToFile = new HttpFetchToFile(url, getJarFile()).onFail(e->logger.error("failed to download jar",
				e
		));
		if(getCredential() != null)
			httpFetchToFile.credentials(new HttpCredentials(getCredential().getUsername(), getCredential().getPassword()));

		Optional<File> result = httpFetchToFile.fetch();
		logger.debug("download jar success: {}", result.isPresent());
	}


	private AbstractURL getUrl(String artifactType)
	{
		AbstractURL abstractServerUrl = new AbstractURL(serverUrl);

		String params = PHR.r("?r={}&g={}&a={}&v={}&p={}", repositoryName, groupId, artifactId, version, artifactType);
		String extension = "service/local/artifact/maven/redirect"+params;

		return new AbstractURL(abstractServerUrl, extension);
	}

	private File getJarFile()
	{
		return new File(getHelperDirectory(), artifactId+".jar");
	}

}
