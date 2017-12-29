package de.domisum.janusinfinifrons.instance;

import de.domisum.lib.auxilium.contracts.Identifyable;
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


	// GETTERS
	public File getRootDirectory()
	{
		return new File(rootDirectory);
	}

}
