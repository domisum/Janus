package de.domisum.janusinfinifrons.component;

import de.domisum.janusinfinifrons.build.ProjectBuild;
import de.domisum.lib.auxilium.contracts.Identifyable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public abstract class JanusComponent implements Identifyable
{

	// PROPERTIES
	@Getter protected final String id;

	// REFERENCES
	@Getter(value = AccessLevel.PROTECTED) private transient File helperDirectory;


	// INIT
	public final void setHelperDirectory(File helperDirectory)
	{
		if(this.helperDirectory != null)
			throw new IllegalStateException("helperDirectory already set, can't be changed after that");

		this.helperDirectory = helperDirectory;
	}

	public abstract void validate();


	// GETTERS
	public abstract String getVersion();


	// UPDATE
	public abstract void update();


	// BUILD
	public abstract void addToBuild(ProjectBuild build);

}
