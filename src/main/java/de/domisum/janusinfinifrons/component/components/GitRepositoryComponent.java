package de.domisum.janusinfinifrons.component.components;

import de.domisum.janusinfinifrons.build.ProjectBuild;
import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.lib.auxilium.util.FileUtil;
import de.domisum.lib.auxilium.util.FileUtil.FileType;
import de.domisum.lib.auxilium.util.file.filter.FilterOutDirectory;
import de.domisum.lib.auxilium.util.java.annotations.InitByDeserialization;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GitRepositoryComponent extends JanusComponent
{

	private static final Logger logger = LoggerFactory.getLogger(GitRepositoryComponent.class);


	// SETTINGS
	@InitByDeserialization private String repositoryUrl;
	@InitByDeserialization private String branch;

	// STATUS
	private transient String latestCommitHash = null;


	// INIT
	@Override public void validate()
	{
		// nothing to validate yet
	}


	// COMPONENT
	@Override public String getVersion()
	{
		if(latestCommitHash == null)
			throw new IllegalStateException("can't get version before first call to #update()");

		return latestCommitHash;
	}

	@Override public void update()
	{
		if(FileUtil.listFiles(getHelperDirectory(), FileType.FILE_AND_DIRECTORY).isEmpty())
			gitClone();

		gitPull();
	}

	@Override public void addToBuild(ProjectBuild build)
	{
		FileUtil.coypDirectory(getHelperDirectory(), build.getDirectory(), new FilterOutDirectory(".git/"));
	}


	// GIT
	private void gitClone()
	{
		logger.info("Cloning git repository '{}' at '{}:{}'", getId(), repositoryUrl, branch);

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
			logger.error("error cloning repository", e);
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
			logger.error("error pulling changes from git repositor", e);
		}

		latestCommitHash = getLatestCommitHash();
	}

	private void injectCredentialsProviderIntoCommand(TransportCommand<?, ?> transportCommand)
	{
		if(getCredential() == null)
			return;

		transportCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(getCredential().getUsername(),
				getCredential().getPassword()));
	}

	private String getLatestCommitHash()
	{
		try(Git git = Git.open(getHelperDirectory()))
		{
			Ref branchRef = git.getRepository().findRef(branch);
			if(branchRef == null)
				throw new IllegalArgumentException("git repository does not contain branch '"+branch+"'");

			return branchRef.getObjectId().getName();
		}
		catch(IOException e)
		{
			logger.error("error reading latest commit hash", e);
			return null;
		}
	}

}
