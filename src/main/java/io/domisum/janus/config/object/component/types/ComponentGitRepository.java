package io.domisum.janus.config.object.component.types;

import io.domisum.janus.config.object.component.Component;
import io.domisum.janus.config.object.component.ComponentDependencyFacade;
import io.domisum.lib.auxiliumlib.PHR;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import io.domisum.lib.auxiliumlib.exceptions.ShouldNeverHappenError;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import io.domisum.lib.auxiliumlib.util.file.filter.FilterOutBaseDirectory;
import io.domisum.lib.auxiliumlib.util.java.ExceptionUtil;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Objects;

public class ComponentGitRepository
		extends Component
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ComponentGitRepository.class);
	
	
	// CONSTANTS
	private static final Duration GIT_CLONE_TIMEOUT = Duration.ofMinutes(5);
	private static final Duration GIT_PULL_TIMEOUT = Duration.ofMinutes(1);
	
	// SETTINGS
	private final String repositoryUrl;
	private final String branch;
	
	// STATUS
	private transient boolean remoteUrlUpdated = false;
	
	
	// INIT
	public ComponentGitRepository(
			String id, String credentialId, ComponentDependencyFacade componentDependencyFacade,
			String repositoryUrl, String branch)
	{
		super(id, credentialId, componentDependencyFacade);
		this.repositoryUrl = repositoryUrl;
		this.branch = branch;
	}
	
	@Override
	public void validateTypeSpecific()
			throws InvalidConfigurationException
	{
		InvalidConfigurationException.validateIsSet(repositoryUrl, "repositoryUrl");
		validateRepositoryUrl();
		InvalidConfigurationException.validateIsSet(branch, "branch");
	}
	
	private void validateRepositoryUrl()
			throws InvalidConfigurationException
	{
		try
		{
			new URIish(repositoryUrl);
		}
		catch(URISyntaxException e)
		{
			throw new InvalidConfigurationException("Invalid repositoryUrl: '"+repositoryUrl+"'", e);
		}
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
		updateRemoteUrlOncePerApplicationRun();
		
		if(doesLocalRepoExist())
			return gitPull();
		else
		{
			gitClone();
			return true;
		}
	}
	
	private boolean doesLocalRepoExist()
			throws IOException
	{
		boolean componentDirectoryEmpty = Files.list(getDirectory().toPath()).findAny().isEmpty();
		return !componentDirectoryEmpty;
	}
	
	
	private void updateRemoteUrlOncePerApplicationRun()
			throws IOException
	{
		if(!doesLocalRepoExist())
			return;
		
		if(remoteUrlUpdated)
			return;
		remoteUrlUpdated = true;
		
		updateRemoteUrl();
	}
	
	private void updateRemoteUrl()
			throws IOException
	{
		try(var git = Git.open(getDirectory()))
		{
			var remoteNames = git.getRepository().getRemoteNames();
			for(String remoteName : remoteNames)
			{
				var remoteSetUrlCommand = git.remoteSetUrl();
				remoteSetUrlCommand.setRemoteName(remoteName);
				remoteSetUrlCommand.setRemoteUri(getRepositoryUriish());
				remoteSetUrlCommand.call();
			}
		}
		catch(GitAPIException e)
		{
			throw new IOException("Gailed to update git remote url in component '"+getId()+"'", e);
		}
	}
	
	private URIish getRepositoryUriish()
	{
		try
		{
			return new URIish(repositoryUrl);
		}
		catch(URISyntaxException e)
		{
			throw new ShouldNeverHappenError("This should have been detected during validation", e);
		}
	}
	
	
	// GIT
	private void gitClone()
			throws IOException
	{
		LOGGER.info("Cloning {}...", this);
		
		try
		{
			var cloneCommand = Git.cloneRepository();
			cloneCommand.setURI(repositoryUrl);
			cloneCommand.setDirectory(getDirectory());
			cloneCommand.setBranch(branch);
			cloneCommand.setTimeout((int) GIT_CLONE_TIMEOUT.getSeconds());
			authorizeCommand(cloneCommand);
			
			cloneCommand.call();
		}
		catch(GitAPIException e)
		{
			throw new IOException("Failed to clone repository in "+this, e);
		}
		
		LOGGER.info("...Cloning done");
	}
	
	private boolean gitPull()
			throws IOException
	{
		try(var git = Git.open(getDirectory()))
		{
			String latestCommitHashBefore = readLatestCommitHash(git);
			
			var pullCommand = git.pull();
			pullCommand.setTimeout((int) GIT_PULL_TIMEOUT.getSeconds());
			authorizeCommand(pullCommand);
			pullCommand.call();
			
			String latestCommitHashAfter = readLatestCommitHash(git);
			return !Objects.equals(latestCommitHashBefore, latestCommitHashAfter);
		}
		catch(GitAPIException e)
		{
			if(shouldUpdateExceptionBeIgnored(e))
				return false;
			
			throw new IOException("Failed to pull changes in "+this, e);
		}
	}
	
	private String readLatestCommitHash(Git git)
			throws IOException
	{
		var branchRef = git.getRepository().findRef(branch);
		if(branchRef == null)
			throw new IllegalArgumentException(PHR.r("Git repository '{}' does not contain branch '{}'", getId(), branch));
		
		return branchRef.getObjectId().getName();
	}
	
	private void authorizeCommand(TransportCommand<?,?> transportCommand)
	{
		if(getCredentialId() != null)
		{
			var credential = getComponentDependencyFacade().getCredential(getCredentialId());
			var gitCredentialsProvider = new UsernamePasswordCredentialsProvider(credential.getUsername(), credential.getPassword());
			transportCommand.setCredentialsProvider(gitCredentialsProvider);
		}
	}
	
	
	// BUILD
	@Override
	public void addToBuild(File directoryInBuild)
	{
		var gitDirFilter = new FilterOutBaseDirectory(".git");
		FileUtil.copyDirectory(getDirectory(), directoryInBuild, gitDirFilter);
	}
	
	
	// UTIL
	private boolean shouldUpdateExceptionBeIgnored(GitAPIException gitApiException)
	{
		String exceptionSynopsisLowerCase = ExceptionUtil.getSynopsis(gitApiException).toLowerCase();
		
		if(StringUtils.containsAny(exceptionSynopsisLowerCase, "connection reset", "authentication not supported"))
			return true;
		if(StringUtils.containsAny(exceptionSynopsisLowerCase, "time out", "timed out", "timeout"))
			return true;
		if(StringUtils.containsAny(exceptionSynopsisLowerCase, "internal server error"))
			return true;
		
		return false;
	}
	
}
