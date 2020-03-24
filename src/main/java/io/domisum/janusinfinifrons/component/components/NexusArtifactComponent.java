package io.domisum.janusinfinifrons.component.components;

import io.domisum.janusinfinifrons.build.ProjectBuild;
import io.domisum.janusinfinifrons.component.CredentialComponent;
import io.domisum.janusinfinifrons.component.JanusComponent;
import io.domisum.janusinfinifrons.project.ProjectComponentDependency;
import io.domisum.lib.auxiliumlib.datacontainers.AbstractURL;
import io.domisum.lib.auxiliumlib.file.FileUtil;
import io.domisum.lib.auxiliumlib.util.java.annotations.InitByDeserialization;
import io.domisum.lib.ezhttp.EzHttpRequestEnvoy;
import io.domisum.lib.ezhttp.header.EzHttpHeaderUsernamePasswordAuthentication;
import io.domisum.lib.ezhttp.request.EzHttpRequest;
import io.domisum.lib.ezhttp.response.EzHttpIoResponse;
import io.domisum.lib.ezhttp.response.EzHttpResponse;
import io.domisum.lib.ezhttp.response.bodyreaders.EzHttpStringBodyReader;
import io.domisum.lib.ezhttp.response.bodyreaders.EzHttpWriteToTempFileBodyReader;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

@NoArgsConstructor
public class NexusArtifactComponent extends JanusComponent implements CredentialComponent
{

	private static final Logger logger = LoggerFactory.getLogger(NexusArtifactComponent.class);


	// SETTINGS
	@InitByDeserialization
	private String repositoryUrl;

	@InitByDeserialization
	private String groupId;
	@InitByDeserialization
	private String artifactId;
	@InitByDeserialization
	private String version;

	// STATUS
	private transient String currentJarIdentifier = null;


	// INIT
	@Override
	public void validate()
	{
		// nothing to validate yet
	}


	// COMPONENT
	private String getArtifactDescription()
	{
		return groupId+":"+artifactId+":"+version+" ("+repositoryUrl+")";
	}

	@Override
	public String getVersion()
	{
		return currentJarIdentifier;
	}

	@Override
	public void update()
	{
		try
		{
			if(version.toUpperCase().endsWith("SNAPSHOT"))
				updateSnapshot();
			else
				updateRelease();
		}
		catch(IOException e)
		{
			logger.error("An error occured while trying to update the nexus artifact component {}", getArtifactDescription(), e);
		}
	}

	private void updateSnapshot() throws IOException
	{
		String artifactVersionDirUrl = repositoryUrl+"/"+groupId.replace(".", "/")+"/"+artifactId+"/"+version;
		String mavenMetadataUrl = artifactVersionDirUrl+"/maven-metadata.xml";

		String mavenMetadata = fetchString(mavenMetadataUrl);
		String snapshotVersion = parseSnapshotVersion(mavenMetadata);
		String jarUrl = artifactVersionDirUrl+"/"+artifactId+"-"+snapshotVersion+".jar";
		handleNewJarIdentifier(jarUrl, snapshotVersion);
	}

	private String parseSnapshotVersion(String mavenMetadata) throws IOException
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder builder = factory.newDocumentBuilder();

			InputStream mavenMetadataInputStream = IOUtils.toInputStream(mavenMetadata, StandardCharsets.UTF_8);
			Document document = builder.parse(mavenMetadataInputStream);

			NodeList snapshotVersions = document.getElementsByTagName("snapshotVersion");
			for(int i = 0; i < snapshotVersions.getLength(); i++)
			{
				Element snapshotVersion = (Element) snapshotVersions.item(i);
				String extension = snapshotVersion.getElementsByTagName("extension").item(0).getTextContent();
				if("jar".equals(extension))
					return snapshotVersion.getElementsByTagName("value").item(0).getTextContent();
			}
		}
		catch(SAXException|ParserConfigurationException e)
		{
			throw new IOException("failed to parse mavenMetadata", e);
		}

		throw new IOException("mavenMetadata did not contain snapshot version");
	}

	private void handleNewJarIdentifier(String jarUrl, String newJarIdentifier) throws IOException
	{
		if(!Objects.equals(currentJarIdentifier, newJarIdentifier))
		{
			logger.info("Detected change for nexus artifact component {}, downloading jar...", getArtifactDescription());
			File fetchedFile = fetchFile(jarUrl);
			logger.info("Nexus artifact jar download complete");

			currentJarIdentifier = newJarIdentifier; // only update this if jar download is successful
			FileUtil.copyFile(fetchedFile, getJarFile());
			FileUtil.delete(fetchedFile);
		}
	}

	private void updateRelease() throws IOException
	{
		String jarUrl = repositoryUrl+"/"+groupId.replace(".", "/")+"/"+artifactId+"/"+version+"/"+artifactId+"-"+version+".jar";
		String jarMd5Url = jarUrl+".md5";

		String jarMd5 = fetchString(jarMd5Url);
		handleNewJarIdentifier(jarUrl, jarMd5);
	}


	@Override
	public void addToBuildThrough(ProjectComponentDependency projectComponentDependency, ProjectBuild build)
	{
		File targetDirectory = new File(build.getDirectory(), projectComponentDependency.getInBuildPath());
		File targetFile = new File(targetDirectory, getJarFile().getName());

		FileUtil.copyFile(getJarFile(), targetFile);
	}


	private File getJarFile()
	{
		return new File(getHelperDirectory(), artifactId+".jar");
	}


	// FETCH
	private String fetchString(String url) throws IOException
	{
		AbstractURL abstractURL = new AbstractURL(url);

		EzHttpRequest request = EzHttpRequest.get(abstractURL);
		authorizeRequest(request);
		EzHttpRequestEnvoy<String> envoy = new EzHttpRequestEnvoy<>(request, new EzHttpStringBodyReader());

		EzHttpIoResponse<String> ioResponse = envoy.send();
		String errorMessage = "failed to fetch string from "+url;
		EzHttpResponse<String> response = ioResponse.getOrThrowWrapped(errorMessage);
		String responseString = response.getSuccessBodyOrThrowHttpIoException(errorMessage);

		return responseString;
	}

	private File fetchFile(String url) throws IOException
	{
		AbstractURL abstractURL = new AbstractURL(url);

		EzHttpRequest request = EzHttpRequest.get(abstractURL);
		authorizeRequest(request);
		EzHttpRequestEnvoy<File> envoy = new EzHttpRequestEnvoy<>(request, new EzHttpWriteToTempFileBodyReader());
		envoy.setTimeout(Duration.ofMinutes(10));

		EzHttpIoResponse<File> ioResponse = envoy.send();
		String errorMessage = "failed to fetch file from "+url;
		EzHttpResponse<File> response = ioResponse.getOrThrowWrapped(errorMessage);
		return response.getSuccessBodyOrThrowHttpIoException(errorMessage);
	}

	private void authorizeRequest(EzHttpRequest request)
	{
		if(getCredential() != null)
		{
			String username = getCredential().getUsername();
			String password = getCredential().getPassword();
			request.addHeader(new EzHttpHeaderUsernamePasswordAuthentication(username, password));
		}
	}

}
