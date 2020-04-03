package io.domisum.janus.config.object.component.types;

import io.domisum.janus.config.object.component.Component;
import io.domisum.janus.config.object.component.ComponentDependencyFacade;
import io.domisum.lib.auxiliumlib.PHR;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import io.domisum.lib.ezhttp.EzHttpRequestEnvoy;
import io.domisum.lib.ezhttp.header.EzHttpHeaderBasicAuthentication;
import io.domisum.lib.ezhttp.request.EzHttpRequest;
import io.domisum.lib.ezhttp.request.url.EzUrl;
import io.domisum.lib.ezhttp.response.bodyreaders.EzHttpStringBodyReader;
import io.domisum.lib.ezhttp.response.bodyreaders.EzHttpWriteToTempFileBodyReader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

public class ComponentMavenArtifactJar
		extends Component
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ComponentMavenArtifactJar.class);
	
	
	// CONSTANTS
	private static final Duration DOWNLOAD_TIMEOUT = Duration.ofMinutes(10);
	
	// SETTINGS
	private final String repositoryUrl;
	private final String groupId;
	private final String artifactId;
	private final String version;
	
	
	// INIT
	public ComponentMavenArtifactJar(
			String id, String credentialId, ComponentDependencyFacade componentDependencyFacade,
			String repositoryUrl, String groupId, String artifactId, String version)
	{
		super(id, credentialId, componentDependencyFacade);
		this.repositoryUrl = repositoryUrl;
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}
	
	@Override
	public void validateTypeSpecific()
			throws InvalidConfigurationException
	{
		InvalidConfigurationException.validateIsSet(repositoryUrl, "repositoryUrl");
		InvalidConfigurationException.validateIsSet(groupId, "groupId");
		InvalidConfigurationException.validateIsSet(artifactId, "artifactId");
		InvalidConfigurationException.validateIsSet(version, "version");
	}
	
	
	// OBJECT
	@Override
	protected String getToStringInfos()
	{
		return groupId+":"+artifactId+":"+version+" in repository '"+repositoryUrl+"'";
	}
	
	
	// UPDATE
	@Override
	public boolean update()
			throws IOException
	{
		if(version.toUpperCase().endsWith("SNAPSHOT"))
			return updateSnapshot();
		else
			return updateRelease();
	}
	
	private boolean updateSnapshot()
			throws IOException
	{
		var repositoryUrl = EzUrl.parseUnescaped(this.repositoryUrl);
		String artifactVersionDirPathExtension = PHR.r("{}/{}/{}", getGroupIdUrlExtension(), artifactId, version);
		var artifactVersionDirUrl = repositoryUrl.withExtendedPath(artifactVersionDirPathExtension);
		
		var mavenMetadataUrl = artifactVersionDirUrl.withExtendedPath("maven-metadata.xml");
		String mavenMetadata = fetchString(mavenMetadataUrl);
		String latestSnapshotBuild = parseLatestSnapshotBuild(mavenMetadata);
		
		var jarUrl = artifactVersionDirUrl.withExtendedPath(artifactId+"-"+latestSnapshotBuild+".jar");
		return downloadJarIfIdentifierChanged(jarUrl, latestSnapshotBuild);
	}
	
	private boolean updateRelease()
			throws IOException
	{
		var repositoryUrl = EzUrl.parseUnescaped(this.repositoryUrl);
		String jarPathExtension = PHR.r("{}/{}/{}/{}-{}.jar", getGroupIdUrlExtension(), artifactId, version, artifactId, version);
		var jarUrl = repositoryUrl.withExtendedPath(jarPathExtension);
		
		var jarMd5Url = EzUrl.parseEscaped(jarUrl.toString()+".md5");
		String jarMd5 = fetchString(jarMd5Url);
		
		return downloadJarIfIdentifierChanged(jarUrl, jarMd5);
	}
	
	private boolean downloadJarIfIdentifierChanged(EzUrl jarUrl, String newJarIdentifier)
			throws IOException
	{
		String previousJarIdentifier = readJarIdentifier();
		boolean newJar = !Objects.equals(previousJarIdentifier, newJarIdentifier);
		
		if(newJar)
		{
			LOGGER.info("Detected change in component '{}', downloading jar...", getId());
			var fetchedFile = fetchFile(jarUrl);
			FileUtil.moveFile(fetchedFile, getJarFile());
			writeJarIdentifier(newJarIdentifier);
			LOGGER.info("...jar download complete");
		}
		
		return newJar;
	}
	
	
	// parsing
	private String parseLatestSnapshotBuild(String mavenMetadata)
			throws IOException
	{
		var document = parseXmlDocument(mavenMetadata, "mavenMetadata");
		var snapshotVersions = document.getElementsByTagName("snapshotVersion");
		for(int i = 0; i < snapshotVersions.getLength(); i++)
		{
			var snapshotVersion = (Element) snapshotVersions.item(i);
			String extension = snapshotVersion.getElementsByTagName("extension").item(0).getTextContent();
			if("jar".equals(extension))
				return snapshotVersion.getElementsByTagName("value").item(0).getTextContent();
		}
		
		throw new IOException("mavenMetadata did not contain latest snapshot build");
	}
	
	private Document parseXmlDocument(String xmlString, String documentName)
			throws IOException
	{
		try
		{
			var documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setIgnoringElementContentWhitespace(true);
			var builder = documentBuilderFactory.newDocumentBuilder();
			
			var mavenMetadataInputStream = IOUtils.toInputStream(xmlString, StandardCharsets.UTF_8);
			return builder.parse(mavenMetadataInputStream);
		}
		catch(SAXException|ParserConfigurationException e)
		{
			throw new IOException("failed to parse "+documentName, e);
		}
	}
	
	
	// fetch
	private String fetchString(EzUrl url)
			throws IOException
	{
		var request = EzHttpRequest.get(url);
		authorizeRequest(request);
		
		var envoy = new EzHttpRequestEnvoy<>(request, new EzHttpStringBodyReader());
		var ioResponse = envoy.send();
		String errorMessage = "failed to fetch string from "+url;
		var response = ioResponse.getOrThrowWrapped(errorMessage);
		String responseString = response.getSuccessBodyOrThrowHttpException(errorMessage);
		
		return responseString;
	}
	
	private File fetchFile(EzUrl url)
			throws IOException
	{
		var request = EzHttpRequest.get(url);
		authorizeRequest(request);
		
		var envoy = new EzHttpRequestEnvoy<>(request, new EzHttpWriteToTempFileBodyReader());
		envoy.setTimeout(DOWNLOAD_TIMEOUT);
		var ioResponse = envoy.send();
		String errorMessage = "failed to fetch file from "+url;
		var response = ioResponse.getOrThrowWrapped(errorMessage);
		
		return response.getSuccessBodyOrThrowHttpException(errorMessage);
	}
	
	private void authorizeRequest(EzHttpRequest request)
	{
		if(getCredentialId() != null)
		{
			var credential = getComponentDependencyFacade().getCredential(getCredentialId());
			var authHeader = new EzHttpHeaderBasicAuthentication(credential.getUsername(), credential.getPassword());
			request.addHeader(authHeader);
		}
	}
	
	
	// BUILD
	@Override
	public void addToBuild(File directoryInBuild)
	{
		var targetFile = new File(directoryInBuild, getJarFile().getName());
		FileUtil.copyFile(getJarFile(), targetFile);
	}
	
	
	// FILE
	private File getJarFile()
	{
		return new File(getDirectory(), artifactId+".jar");
	}
	
	private File getJarIdentifierFile()
	{
		return new File(getDirectory(), "identifier.txt");
	}
	
	private void writeJarIdentifier(String jarIdentifier)
	{
		FileUtil.writeString(getJarIdentifierFile(), jarIdentifier);
	}
	
	private String readJarIdentifier()
	{
		File file = getJarIdentifierFile();
		
		if(!file.exists())
			return null;
		return FileUtil.readString(file);
	}
	
	
	// UTIL
	private String getGroupIdUrlExtension()
	{
		return groupId.replace(".", "/");
	}
	
}
