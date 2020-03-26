package io.domisum.janus.component.components;

import io.domisum.janus.ValidationReport;
import io.domisum.janus.component.JanusComponent;
import io.domisum.janus.component.JanusComponentDependencies;
import io.domisum.janus.project.JanusProjectBuild;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import io.domisum.lib.auxiliumlib.util.file.FileUtil.FileType;
import io.domisum.lib.auxiliumlib.util.file.filter.FilterOutBaseDirectory;
import org.apache.commons.lang3.Validate;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

public class JanusComponentGitRepository
		extends JanusComponent
{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	// CONSTANTS
	private static final Duration GIT_COMMAND_TIMEOUT = Duration.ofMinutes(5);
	
	// DEPENDENCIES
	private final JanusComponentDependencies janusComponentDependencies;
	
	// SETTINGS
	private final String repositoryUrl;
	private final String branch;
	
	// STATUS
	private transient String latestCommitHash = null;
	
	
	// INIT
	public JanusComponentGitRepository(
			JanusComponentDependencies janusComponentDependencies,
			String id, String credentialId, String directoryInBuild,
			String repositoryUrl, String branch)
	{
		super(id, credentialId, directoryInBuild);
		this.janusComponentDependencies = janusComponentDependencies;
		this.repositoryUrl = repositoryUrl;
		this.branch = branch;
	}
	
	
	// COMPONENT
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
		var filesInComponentDirectory = FileUtil.listFilesFlat(getDirectory(), FileType.FILE_AND_DIRECTORY);
		if(filesInComponentDirectory.isEmpty())
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
		var cloneCommand = Git.cloneRepository();
		cloneCommand.setURI(repositoryUrl);
		cloneCommand.setDirectory(getDirectory());
		cloneCommand.setBranch(branch);
		cloneCommand.setTimeout((int) GIT_COMMAND_TIMEOUT.getSeconds());
		authorizeCommand(cloneCommand);
		
		logger.info("Cloning {}...", this);
		try(var git = cloneCommand.call())
		{
			updateLatestCommitHash(git);
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
			var pullCommand = git.pull();
			pullCommand.setTimeout((int) GIT_COMMAND_TIMEOUT.getSeconds());
			authorizeCommand(pullCommand);
			pullCommand.call();
			
			return updateLatestCommitHash(git);
		}
		catch(GitAPIException e)
		{
			throw new IOException("failed to pull changes in "+this, e);
		}
	}
	
	private void authorizeCommand(TransportCommand<?,?> transportCommand)
	{
		if(getCredentialId() != null)
		{
			var credential = janusComponentDependencies.getCredential(getCredentialId());
			var gitCredentialsProvider = new UsernamePasswordCredentialsProvider(credential.getUsername(), credential.getPassword());
			transportCommand.setCredentialsProvider(gitCredentialsProvider);
		}
	}
	
	private boolean updateLatestCommitHash(Git git)
			throws IOException
	{
		var branchRef = git.getRepository().findRef(branch);
		if(branchRef == null)
			throw new IllegalArgumentException("git repository does not contain branch '"+branch+"'");
		
		String newLatestCommitHash = branchRef.getObjectId().getName();
		boolean changed = !Objects.equals(latestCommitHash, newLatestCommitHash);
		latestCommitHash = newLatestCommitHash;
		
		return changed;
	}
	
	
	// BUILD
	@Override
	public void addToBuild(JanusProjectBuild build)
	{
		var gitDirFilter = new FilterOutBaseDirectory(".git");
		
		var targetDirectory = getDirectoryInBuild(build);
		FileUtil.copyDirectory(getDirectory(), targetDirectory, gitDirFilter);
	}
	
}
