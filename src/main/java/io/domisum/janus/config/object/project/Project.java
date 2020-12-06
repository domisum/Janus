package io.domisum.janus.config.object.project;

import io.domisum.janus.api.JanusApiUsingFiles;
import io.domisum.lib.auxiliumlib.PHR;
import io.domisum.lib.auxiliumlib.config.ConfigException;
import io.domisum.lib.auxiliumlib.config.ConfigObject;
import io.domisum.lib.auxiliumlib.util.StringListUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Project
	extends ConfigObject
{
	
	// CONSTANTS
	public static final String LATEST_BUILD_FILE_NAME = JanusApiUsingFiles.LATEST_BUILD_FILE_NAME;
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
		throws ConfigException
	{
		ConfigException.validateIsSet(id, "id");
		
		if(isJanusJar())
			ConfigException.validateIsTrue(buildRootDirectory == null && exportDirectory == null,
				"'buildRootDirectory' and 'exportDirectory' can't be set for janus jar project");
		else if(isJanusConfig())
			ConfigException.validateIsTrue(buildRootDirectory == null && exportDirectory == null,
				"'buildRootDirectory' and 'exportDirectory' can't be set for janus config project");
		else
			ConfigException.validateIsTrue(!(buildRootDirectory == null && exportDirectory == null),
				"Either 'buildRootDirectory' or 'exportDirectory' has to be set");
		
		if(buildRootDirectory != null)
		{
			validatePath(buildRootDirectory, "buildRootDirectory");
			ConfigException.validateIsTrue(id.equalsIgnoreCase(getBuildRootDirectory().getName()),
				"The name of 'buildRootDirectory' has to be the id of the project");
		}
		
		if(exportDirectory != null)
			validatePath(exportDirectory, "exportDirectory");
		
		if(exportDirectory == null)
			ConfigException.validateIsTrue(keepOtherFilesOnExport == null,
				"'keepOtherFilesOnExport' is only supported for projects which define 'exportDirectory'");
		
		validateComponents();
	}
	
	private void validatePath(String path, String pathName)
		throws ConfigException
	{
		try
		{
			parseConfigPath(path);
		}
		catch(IllegalArgumentException e)
		{
			throw new ConfigException("Invalid value for "+pathName, e);
		}
	}
	
	private void validateComponents()
		throws ConfigException
	{
		for(int i = 0; i < components.size(); i++)
		{
			var projectComponent = components.get(i);
			try
			{
				projectComponent.validate();
				projectDependencyFacade.validateComponentExists(projectComponent.getComponentId());
			}
			catch(ConfigException e)
			{
				throw new ConfigException("configuration error in projectComponent at index "+i, e);
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
		String componentDisplay = StringListUtil.list(componentIds);
		
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
		
		return keepOtherFilesOnExport != null && keepOtherFilesOnExport;
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
			throws ConfigException
		{
			ConfigException.validateIsSet(componentId, "componentId");
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
		path = replacePathVar(path, "(?i)^%appdata%", System.getenv("APPDATA"));
		path = replacePathVar(path, "(?i)^%home%", System.getProperty("user.home"));
		path = replacePathVar(path, "^~", System.getProperty("user.home"));
		
		return new File(path);
	}
	
	private static String replacePathVar(String path, String regex, String replacement)
	{
		if(replacement == null && path.matches(regex))
			throw new IllegalArgumentException("Can't replace variable, this variable is not set for this system. Path: "+path);
		else if(replacement != null)
		{
			replacement = replacement.replace('\\', '/');
			path = path.replaceFirst(regex, replacement);
		}
		
		return path;
	}
	
}
