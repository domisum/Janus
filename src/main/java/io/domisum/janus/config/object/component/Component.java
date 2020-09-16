package io.domisum.janus.config.object.component;

import io.domisum.lib.auxiliumlib.config.ConfigException;
import io.domisum.lib.auxiliumlib.config.ConfigObject;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
public abstract class Component
	extends ConfigObject
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
		throws ConfigException
	{
		ConfigException.validateNotBlank(id, "id can't be null");
		if(credentialId != null)
			componentDependencyFacade.validateCredentialExists(credentialId);
		validateTypeSpecific();
	}
	
	protected abstract void validateTypeSpecific()
		throws ConfigException;
	
	
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
	
	public abstract String getFingerprint();
	
	public abstract void addToBuild(File directoryInBuild);
	
	
	// UTIL
	public File getDirectory()
	{
		var directory = new File(COMPONENTS_DIRECTORY, getId());
		FileUtil.mkdirs(directory);
		return directory;
	}
	
}
