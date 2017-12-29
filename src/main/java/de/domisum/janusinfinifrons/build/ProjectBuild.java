package de.domisum.janusinfinifrons.build;

import de.domisum.janusinfinifrons.project.JanusProject;
import de.domisum.lib.auxilium.contracts.storage.Keyable;
import de.domisum.lib.auxilium.util.FileUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.File;

@RequiredArgsConstructor
@EqualsAndHashCode(of = {"project", "buildId"})
@ToString(of = {"project", "buildId"})
public class ProjectBuild implements Keyable<JanusProject>
{

	private final JanusProject project;
	private final String buildId;

	@Getter private final File directory;


	// FILES
	public void delete()
	{
		FileUtil.deleteDirectory(directory);
	}


	// STORAGE
	@Override public JanusProject getKey()
	{
		return project;
	}

}
