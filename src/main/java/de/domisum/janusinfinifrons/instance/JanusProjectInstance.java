package de.domisum.janusinfinifrons.instance;

import de.domisum.janusinfinifrons.project.JanusProject;
import de.domisum.lib.auxilium.contracts.Identifyable;
import de.domisum.lib.auxilium.contracts.source.optional.FiniteOptionalSource;
import de.domisum.lib.auxilium.util.FileUtil;
import de.domisum.lib.auxilium.util.PHR;
import de.domisum.lib.auxilium.util.java.exceptions.InvalidConfigurationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class JanusProjectInstance implements Identifyable
{

	@Getter private final String id;
	@Getter private final String projectId;

	private final String rootDirectory;


	// INIT
	public void validate(FiniteOptionalSource<String, JanusProject> projectSource)
	{
		if(!projectSource.contains(projectId))
			throw new InvalidConfigurationException(PHR.r("instance '{}' specifies unknown project: {}", id, projectId));
	}


	// GETTERS
	public File getRootDirectory()
	{
		return new File(rootDirectory);
	}


	// FILE
	public void writeLatestBuildId(String buildId)
	{
		File latestMarkerFile = new File(getRootDirectory(), "latest");
		FileUtil.writeString(latestMarkerFile, buildId);
	}

}
