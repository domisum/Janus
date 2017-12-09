package de.domisum.janusinfinifrons.component.components;

import de.domisum.janusinfinifrons.component.CredentialComponent;
import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.janusinfinifrons.credential.Credential;
import de.domisum.lib.auxilium.util.FileUtil;
import lombok.Getter;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class GitRepositoryComponent extends JanusComponent implements CredentialComponent
{

	private static Logger logger = LoggerFactory.getLogger(GitRepositoryComponent.class);


	// SETTINGS
	private final String repositoryUrl;
	private final String branch;
	@Getter private final String credentialId;

	// REFERENCES
	private transient Credential credential;


	// INIT
	public GitRepositoryComponent(String id, String repositoryUrl, String branch, String credentialId)
	{
		super(id);

		this.repositoryUrl = repositoryUrl;
		this.branch = branch;
		this.credentialId = credentialId;
	}

	@Override public void injectCredential(Credential credential)
	{
		if(this.credential != null)
			throw new IllegalStateException("credential is already set, can't change after that");

		this.credential = credential;
	}


	// GETTERS
	@Override public String getVersion()
	{
		return null;
	}


	// UPDATE
	@Override public void update()
	{
		if(FileUtil.isDirectoryEmpty(getHelperDirectory()))
			gitClone();
		else
			gitPull();
	}

	@Override protected void addToBuild(File buildDir)
	{

	}


	// GIT
	private void gitClone()
	{
		logger.info("Cloning git repository '{}' at '{}:{}'", id, repositoryUrl, branch);

		CloneCommand cloneCommand = Git.cloneRepository();
		cloneCommand.setURI(repositoryUrl);
		cloneCommand.setDirectory(getHelperDirectory());
		cloneCommand.setBranch(branch);
		injectCredentialsProviderIntoCommand(cloneCommand);

		try
		{
			Git git = cloneCommand.call();
			git.close();
		}
		catch(GitAPIException e)
		{
			e.printStackTrace();
		}

		logger.info("Cloning done");
	}

	private void gitPull()
	{
		try(Git git = Git.open(getHelperDirectory()))
		{
			PullCommand pullCommand = git.pull();
			injectCredentialsProviderIntoCommand(pullCommand);

			pullCommand.call();
		}
		catch(IOException|GitAPIException e)
		{
			e.printStackTrace();
		}
	}

	private void injectCredentialsProviderIntoCommand(TransportCommand transportCommand)
	{
		if(credential == null)
			return;

		transportCommand.setCredentialsProvider(
				new UsernamePasswordCredentialsProvider(credential.getUsername(), credential.getPassword()));
	}

}
