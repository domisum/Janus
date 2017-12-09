package de.domisum.janusinfinifrons.component.components;

import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.lib.auxilium.data.container.AbstractURL;
import de.domisum.lib.auxilium.util.PHR;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Objects;

public class NexusArtifactComponent extends JanusComponent
{

	private static Logger logger = LoggerFactory.getLogger(NexusArtifactComponent.class);


	// SETTINGS
	private final String serverUrl;
	private final String repositoryName;

	private final String groupId;
	private final String artifactId;
	private final String version;

	// TEMP
	private transient String currentJarMd5 = null;


	// INIT
	public NexusArtifactComponent(String id, String serverUrl, String repositoryName, String groupId, String artifactId,
			String version)
	{
		super(id);

		this.serverUrl = serverUrl;
		this.repositoryName = repositoryName;

		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}


	// GETTERS
	@Override public String getVersion()
	{
		if(currentJarMd5 == null)
			throw new IllegalStateException("can't check version before first update");

		return currentJarMd5;
	}


	// UPDATE
	@Override protected void update()
	{
		String lastJarMd5 = currentJarMd5;
		currentJarMd5 = fetchJarMd5();

		boolean md5Changed = !Objects.equals(lastJarMd5, currentJarMd5);
		if(md5Changed)
			downloadJar();
	}

	@Override protected void addToBuild(File buildDir)
	{

	}


	// FETCH
	private String fetchJarMd5()
	{
		AbstractURL jarMd5Url = getUrl("jar.md5");

		try
		{
			return IOUtils.toString(jarMd5Url.toNet(), Charset.forName("UTF-8"));
		}
		catch(FileNotFoundException e)
		{
			logger.error("Could not find artifact '{}.{}:{}' in repository '{}'@'{}'", groupId, artifactId, version,
					repositoryName, serverUrl);

			throw new UncheckedIOException(e);
		}
		catch(IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private void downloadJar()
	{
		AbstractURL jarUrl = getUrl("jar");
		File downloadTo = new File(getHelperDirectory(), artifactId+".jar");

		try
		{
			FileUtils.copyURLToFile(jarUrl.toNet(), downloadTo, 5*1000, 60*1000);
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

}
