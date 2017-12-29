package de.domisum.janusinfinifrons.component;

import de.domisum.janusinfinifrons.build.ProjectBuild;
import de.domisum.janusinfinifrons.credential.Credential;
import de.domisum.lib.auxilium.contracts.Identifyable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.File;

@EqualsAndHashCode(of = "id")
public abstract class JanusComponent implements Identifyable
{

	// PROPERTIES
	@Getter private String id;
	@Getter private String credentialId;

	// REFERENCES
	@Getter(AccessLevel.PROTECTED) private transient Credential credential;
	@Getter(AccessLevel.PROTECTED) private transient File helperDirectory;


	// INIT
	public abstract void validate();

	public void injectCredential(Credential credential)
	{
		if(this.credential != null)
			throw new IllegalStateException("credential is already set, can't change after that");

		this.credential = credential;
	}

	public final void setHelperDirectory(File helperDirectory)
	{
		if(this.helperDirectory != null)
			throw new IllegalStateException("helperDirectory already set, can't be changed after that");

		this.helperDirectory = helperDirectory;
	}


	// COMPONENT
	public abstract String getVersion();

	public abstract void update();

	public abstract void addToBuild(ProjectBuild build);

}
