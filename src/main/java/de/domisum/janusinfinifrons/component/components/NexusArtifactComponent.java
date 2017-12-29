package de.domisum.janusinfinifrons.component.components;

import de.domisum.janusinfinifrons.build.ProjectBuild;
import de.domisum.janusinfinifrons.component.CredentialComponent;
import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.lib.auxilium.data.container.AbstractURL;
import de.domisum.lib.auxilium.util.FileUtil;
import de.domisum.lib.auxilium.util.PHR;
import de.domisum.lib.auxilium.util.http.HttpCredentials;
import de.domisum.lib.auxilium.util.http.HttpFetch;
import de.domisum.lib.auxilium.util.http.specific.HttpFetchString;
import de.domisum.lib.auxilium.util.java.annotations.InitByDeserialization;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor
public class NexusArtifactComponent extends JanusComponent implements CredentialComponent
{

	private static final Logger logger = LoggerFactory.getLogger(NexusArtifactComponent.class);


	// CONSTANTS
	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
	private static final Duration DOWNLOAD_TIMEOUT = Duration.ofSeconds(60);

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
		currentJarMd5 = fetchJarMd5();

		boolean md5Changed = !Objects.equals(lastJarMd5, currentJarMd5);
		if(md5Changed)
			downloadJar();
	}

	@Override public void addToBuild(ProjectBuild build)
	{
		FileUtil.copyFile(getJarFile(), build.getDirectory());
	}


	// FETCH
	private String fetchJarMd5()
	{
		AbstractURL url = getUrl("jar.md5");

		HttpFetch<String> fetchString = new HttpFetchString(url).onFail(e->logger.error("failed to fetch jar md5", e));
		if(getCredential() != null)
			fetchString.credentials(new HttpCredentials(getCredential().getUsername(), getCredential().getPassword()));


		Optional<String> jarMd5Optional = fetchString.fetch();
		if(!jarMd5Optional.isPresent())
			throw new UncheckedIOException(new FileNotFoundException(url.toString()));

		return jarMd5Optional.get();
	}

	private void downloadJar()
	{
		try
		{
			FileUtils.copyURLToFile(getUrl("jar").toNet(),
					getJarFile(),
					(int) CONNECT_TIMEOUT.toMillis(),
					(int) DOWNLOAD_TIMEOUT.toMillis());
		}
		catch(IOException e)
		{
			throw new UncheckedIOException(e);
		}
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
