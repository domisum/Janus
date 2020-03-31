package io.domisum.janus.config.object.project;

import io.domisum.janus.config.object.ConfigObject;
import io.domisum.lib.auxiliumlib.PHR;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import io.domisum.lib.auxiliumlib.util.StringUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Project
		implements ConfigObject
{
	
	// CONSTANTS
	public static final String LATEST_BUILD_FILE_NAME = "latestBuild.txt";
	public static final String RUNNINTG_BUILD_FILE_NAME = "runningBuild.txt";
	
	// ATTRIBUTES
	@Getter
	private final String id;
	
	private final String buildRootDirectory;
	
	private final String exportDirectory;
	private final Boolean keepOtherFilesOnExport;
	
	private final String commandToExecuteAfterBuild;
	
	// COMPONENTS
	private final List<ProjectComponent> components;
	
	// DEPENDENCY
	private final ProjectDependencyFacade projectDependencyFacade;
	
	
	// INIT
	@Override
	public void validate()
			throws InvalidConfigurationException
	{
		InvalidConfigurationException.validateIsSet(id, "id");
		
		if(isJanusJar())
			InvalidConfigurationException.validateIsTrue(buildRootDirectory == null && exportDirectory == null,
					"'buildRootDirectory' and 'exportDirectory' can't be set for janus jar project");
		else if(isJanusConfig())
			InvalidConfigurationException.validateIsTrue(buildRootDirectory == null && exportDirectory == null,
					"'buildRootDirectory' and 'exportDirectory' can't be set for janus config project");
		else
			InvalidConfigurationException.validateIsTrue(!(buildRootDirectory == null && exportDirectory == null),
					"Either 'buildRootDirectory' or 'exportDirectory' has to be set");
		
		if(buildRootDirectory != null)
			InvalidConfigurationException.validateIsTrue(id.equalsIgnoreCase(getBuildRootDirectory().getName()),
					"The name of 'buildRootDirectory' has to be the id of the project");
		
		if(exportDirectory == null)
			InvalidConfigurationException.validateIsTrue(keepOtherFilesOnExport == null,
					"'keepOtherFilesOnExport' is only supported for projects which define 'exportDirectory'");
		
		validateComponents();
	}
	
	private void validateComponents()
			throws InvalidConfigurationException
	{
		for(int i = 0; i < components.size(); i++)
		{
			var projectComponent = components.get(i);
			try
			{
				projectComponent.validate();
				projectDependencyFacade.validateComponentExists(projectComponent.getComponentId());
			}
			catch(InvalidConfigurationException e)
			{
				throw new InvalidConfigurationException("configuration error in projectComponent at index "+i, e);
			}
		}
	}
	
	
	// OBJECT
	@Override
	public String toString()
	{
		var componentIds = new ArrayList<>();
		for(var component : components)
			componentIds.add(component.getComponentId());
		String componentDisplay = StringUtil.listToString(componentIds, ", ");
		
		return PHR.r("Project({}: components=({}))", id, componentDisplay);
	}
	
	
	// GETTERS
	public File getBuildRootDirectory()
	{
		if(buildRootDirectory == null)
			return null;
		return parseConfigPath(buildRootDirectory);
	}
	
	public File getExportDirectory()
	{
		if(exportDirectory == null)
			return null;
		return parseConfigPath(exportDirectory);
	}
	
	public boolean isJanusJar()
	{
		return "___janusJar".equals(id);
	}
	
	public boolean isJanusConfig()
	{
		return "___janusConfig".equals(id);
	}
	
	public boolean keepOtherFilesOnExport()
	{
		if(exportDirectory == null)
			throw new UnsupportedOperationException("This method only works if this is an export project");
		
		return keepOtherFilesOnExport == null ? false : keepOtherFilesOnExport;
	}
	
	public String getCommandToExecuteAfterBuild()
	{
		return commandToExecuteAfterBuild;
	}
	
	
	public List<ProjectComponent> getComponents()
	{
		return new ArrayList<>(components);
	}
	
	
	// COMPONENT
	@RequiredArgsConstructor
	public static class ProjectComponent
	{
		
		@Getter
		private final String componentId;
		private final String directoryInBuild;
		
		
		// INIT
		public void validate()
				throws InvalidConfigurationException
		{
			InvalidConfigurationException.validateIsSet(componentId, "componentId");
		}
		
		
		// GETTERS
		public File getDirectoryInBuild(File buildDirectory)
		{
			if(directoryInBuild == null)
				return buildDirectory;
			return new File(buildDirectory, directoryInBuild);
		}
		
	}
	
	
	// UTIL
	private static File parseConfigPath(String path)
	{
		path = path.replaceFirst("(?i)^%APPDATA%$", System.getenv("APPDATA"));
		path = path.replaceFirst("(?i)^%HOME%$", System.getProperty("user.home"));
		path = path.replaceFirst("^~$", System.getProperty("user.home"));
		
		return new File(path);
	}
	
}
