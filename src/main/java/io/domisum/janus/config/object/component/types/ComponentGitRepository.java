package io.domisum.janus.config.object.component.types;

import io.domisum.janus.config.object.ValidationReport;
import io.domisum.janus.config.object.component.Component;
import io.domisum.janus.config.object.component.ComponentDependencies;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import io.domisum.lib.auxiliumlib.util.file.filter.FilterOutBaseDirectory;
import org.apache.commons.lang3.Validate;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Objects;

public class ComponentGitRepository
		extends Component
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// CONSTANTS
	private static final Duration GIT_COMMAND_TIMEOUT = Duration.ofMinutes(5);
	
	// SETTINGS
	private final String repositoryUrl;
	private final String branch;
	
	
	// INIT
	public ComponentGitRepository(
			String id, String credentialId, ComponentDependencies componentDependencies,
			String repositoryUrl, String branch)
	{
		super(id, credentialId, componentDependencies);
		this.repositoryUrl = repositoryUrl;
		this.branch = branch;
	}
	
	@Override
	public void validateTypeSpecific(ValidationReport validationReport)
	{
		Validate.notNull(repositoryUrl);
		Validate.notNull(branch);
	}
	
	
	// OBJECT
	@Override
	protected String getToStringInfos()
	{
		return "'"+repositoryUrl+"':"+branch;
	}
	
	
	// UPDATE
	@Override
	public boolean update()
			throws IOException
	{
		boolean componentDirectoryEmpty = Files.list(getDirectory().toPath()).findAny().isEmpty();
		if(componentDirectoryEmpty)
		{
			gitClone();
			return true;
		}
		else
			return gitPull();
	}
	
	
	// GIT
	private void gitClone()
			throws IOException
	{
		logger.info("Cloning {}...", this);
		
		try
		{
			var cloneCommand = Git.cloneRepository();
			cloneCommand.setURI(repositoryUrl);
			cloneCommand.setDirectory(getDirectory());
			cloneCommand.setBranch(branch);
			cloneCommand.setTimeout((int) GIT_COMMAND_TIMEOUT.getSeconds());
			authorizeCommand(cloneCommand);
			
			cloneCommand.call();
		}
		catch(GitAPIException e)
		{
			throw new IOException("failed to clone repository in "+this, e);
		}
		
		logger.info("...Cloning done");
	}
	
	private boolean gitPull()
			throws IOException
	{
		try(var git = Git.open(getDirectory()))
		{
			String latestCommitHashBefore = readLatestCommitHash(git);
			
			var pullCommand = git.pull();
			pullCommand.setTimeout((int) GIT_COMMAND_TIMEOUT.getSeconds());
			authorizeCommand(pullCommand);
			pullCommand.call();
			
			String latestCommitHashAfter = readLatestCommitHash(git);
			return !Objects.equals(latestCommitHashBefore, latestCommitHashAfter);
		}
		catch(GitAPIException e)
		{
			throw new IOException("failed to pull changes in "+this, e);
		}
	}
	
	private String readLatestCommitHash(Git git)
			throws IOException
	{
		var branchRef = git.getRepository().findRef(branch);
		if(branchRef == null)
			throw new IllegalArgumentException("git repository does not contain branch '"+branch+"'");
		
		return branchRef.getObjectId().getName();
	}
	
	private void authorizeCommand(TransportCommand<?,?> transportCommand)
	{
		if(getCredentialId() != null)
		{
			var credential = getComponentDependencies().getCredential(getCredentialId());
			var gitCredentialsProvider = new UsernamePasswordCredentialsProvider(credential.getUsername(), credential.getPassword());
			transportCommand.setCredentialsProvider(gitCredentialsProvider);
		}
	}
	
	
	// BUILD
	@Override
	public void addToBuild(File inBuildDir)
	{
		var gitDirFilter = new FilterOutBaseDirectory(".git");
		FileUtil.copyDirectory(getDirectory(), inBuildDir, gitDirFilter);
	}
	
}