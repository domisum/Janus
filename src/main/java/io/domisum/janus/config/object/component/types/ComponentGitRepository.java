package io.domisum.janus.config.object.component.types;

import io.domisum.janus.config.object.component.Component;
import io.domisum.janus.config.object.component.ComponentDependencyFacade;
import io.domisum.lib.auxiliumlib.PHR;
import io.domisum.lib.auxiliumlib.config.ConfigException;
import io.domisum.lib.auxiliumlib.exceptions.ProgrammingError;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import io.domisum.lib.auxiliumlib.util.file.filter.FilterOutBaseDirectory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Objects;

public class ComponentGitRepository
	extends Component
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ComponentGitRepository.class);
	
	
	// CONSTANTS
	private static final Duration GIT_CLONE_TIMEOUT = Duration.ofMinutes(5);
	private static final Duration GIT_PULL_TIMEOUT = Duration.ofSeconds(30);
	
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
		throws ConfigException
	{
		ConfigException.validateIsSet(repositoryUrl, "repositoryUrl");
		validateRepositoryUrl();
		ConfigException.validateIsSet(branch, "branch");
	}
	
	private void validateRepositoryUrl()
		throws ConfigException
	{
		try
		{
			new URIish(repositoryUrl);
		}
		catch(URISyntaxException e)
		{
			throw new ConfigException("Invalid repositoryUrl: '"+repositoryUrl+"'", e);
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
		updateRemoteUrlIfNotDoneAlready();
		
		if(doesLocalRepoExist())
			return gitPull();
		else
		{
			gitClone();
			return true;
		}
	}
	
	private boolean doesLocalRepoExist()
	{
		boolean componentDirectoryEmpty = FileUtil.isDirectoryEmpty(getDirectory());
		return !componentDirectoryEmpty;
	}
	
	
	private void updateRemoteUrlIfNotDoneAlready()
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
			throw new ProgrammingError("This should have been detected during validation", e);
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
		
		try(var git = Git.open(getDirectory()))
		{
			LOGGER.info("...Cloning complete. Latest commit: ({})", getLatestCommitDisplay(git));
		}
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
			boolean changed = !Objects.equals(latestCommitHashBefore, latestCommitHashAfter);
			if(changed)
				LOGGER.info("GitRepository component '{}' pulled changes. Last commit: ({})", getId(), getLatestCommitDisplay(git));
			
			return changed;
		}
		catch(GitAPIException e)
		{
			throw new IOException("Failed to pull changes in "+this, e);
		}
	}
	
	private String readLatestCommitHash(Git git)
		throws IOException
	{
		var branchRef = findBranchRef(git);
		return branchRef.getObjectId().getName();
	}
	
	private String getLatestCommitDisplay(Git git)
		throws IOException
	{
		var branchRef = findBranchRef(git);
		var latestCommit = git.getRepository().parseCommit(branchRef.getObjectId());
		
		return branchRef.getObjectId().getName()+": '"+latestCommit.getShortMessage()+"'";
	}
	
	private Ref findBranchRef(Git git)
		throws IOException
	{
		var branchRef = git.getRepository().findRef(branch);
		if(branchRef == null)
			throw new IllegalArgumentException(PHR.r("Git repository '{}' does not contain branch '{}'", getId(), branch));
		return branchRef;
	}
	
	private void authorizeCommand(TransportCommand<?, ?> transportCommand)
	{
		if(getCredentialId() != null)
		{
			var credential = getComponentDependencyFacade().getCredential(getCredentialId());
			var gitCredentialsProvider = new UsernamePasswordCredentialsProvider(credential.getUsername(), credential.getPassword());
			transportCommand.setCredentialsProvider(gitCredentialsProvider);
		}
	}
	
	
	// FINGERPRINT
	@Override
	public String getFingerprint()
	{
		try(var git = Git.open(getDirectory()))
		{
			String latestCommitHash = readLatestCommitHash(git);
			return latestCommitHash;
		}
		catch(IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
	
	
	// BUILD
	@Override
	public void addToBuild(File directoryInBuild)
	{
		var gitDirFilter = new FilterOutBaseDirectory(".git");
		FileUtil.copyDirectory(getDirectory(), directoryInBuild, gitDirFilter);
	}
	
}
