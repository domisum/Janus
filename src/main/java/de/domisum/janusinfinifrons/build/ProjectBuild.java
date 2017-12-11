package de.domisum.janusinfinifrons.build;

import de.domisum.janusinfinifrons.project.JanusProject;
import de.domisum.lib.auxilium.util.FileUtil;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.time.Instant;

@RequiredArgsConstructor
@EqualsAndHashCode(of = {"project", "time"})
public class ProjectBuild
{

	private final JanusProject project;
	private final Instant time;

	private final File directory;


	// FILES
	public void delete()
	{
		FileUtil.deleteDirectory(directory);
	}

}
