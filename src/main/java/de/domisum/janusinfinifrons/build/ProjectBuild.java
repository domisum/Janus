package de.domisum.janusinfinifrons.build;

import de.domisum.janusinfinifrons.project.JanusProject;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.time.Instant;

@RequiredArgsConstructor
@EqualsAndHashCode(of = {"project", "time"})
public class ProjectBuild implements Comparable<ProjectBuild>
{

	private final JanusProject project;
	private final Instant time;

	private final File directory;


	// OBJECT
	@Override public int compareTo(ProjectBuild other)
	{
		return time.compareTo(other.time);
	}


	// FILES
	public void delete()
	{

	}

}
