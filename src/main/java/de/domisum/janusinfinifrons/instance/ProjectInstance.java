package de.domisum.janusinfinifrons.instance;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class ProjectInstance
{

	@Getter private final String id;
	@Getter private final String projectId;
	private final String rootDirectory;


	// GETTERS
	public File getRootDirectory()
	{
		return new File(rootDirectory);
	}

}
