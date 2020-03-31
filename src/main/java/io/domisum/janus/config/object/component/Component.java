package io.domisum.janus.config.object.component;

import io.domisum.janus.config.object.ConfigObject;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
public abstract class Component
		implements ConfigObject
{
	
	// CONSTANTS
	public static final File COMPONENTS_DIRECTORY = new File("_components");
	
	// ATTRIBUTES
	@Getter
	private final String id;
	@Getter
	private final String credentialId;
	
	// DEPENDENCY
	@Getter(AccessLevel.PROTECTED)
	private final ComponentDependencyFacade componentDependencyFacade;
	
	
	// INIT
	@Override
	public void validate()
			throws InvalidConfigurationException
	{
		InvalidConfigurationException.validateIsSet(id, "id can't be null");
		if(credentialId != null)
			componentDependencyFacade.validateCredentialExists(credentialId);
		validateTypeSpecific();
	}
	
	protected abstract void validateTypeSpecific()
			throws InvalidConfigurationException;
	
	
	// OBJECT
	@Override
	public String toString()
	{
		return getClass().getSimpleName()+"("+getId()+": "+getToStringInfos()+")";
	}
	
	protected abstract String getToStringInfos();
	
	
	// COMPONENT
	public abstract boolean update()
			throws IOException;
	
	public abstract void addToBuild(File directoryInBuild);
	
	
	// UTIL
	public File getDirectory()
	{
		var directory = new File(COMPONENTS_DIRECTORY, getId());
		FileUtil.mkdirs(directory);
		return directory;
	}
	
}
