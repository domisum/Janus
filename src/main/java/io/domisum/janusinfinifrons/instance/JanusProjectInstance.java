package io.domisum.janusinfinifrons.instance;

import io.domisum.janusinfinifrons.project.JanusProject;
import io.domisum.lib.auxiliumlib.contracts.Identifyable;
import io.domisum.lib.auxiliumlib.contracts.source.optional.FiniteOptionalSource;
import io.domisum.lib.auxiliumlib.file.FileUtil;
import io.domisum.lib.auxiliumlib.util.PHR;
import io.domisum.lib.auxiliumlib.util.java.exceptions.InvalidConfigurationException;
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
