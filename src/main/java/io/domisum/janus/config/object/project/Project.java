package io.domisum.janus.config.object.project;

import io.domisum.janus.config.object.ConfigObject;
import io.domisum.janus.config.object.ValidationReport;
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
	
	// COMPONENTS
	private final List<ProjectComponent> components;
	
	// DEPENDENCY
	private final ProjectDependencyFacade projectDependencyFacade;
	
	
	// INIT
	@Override
	public ValidationReport validate()
			throws InvalidConfigurationException
	{
		var validationReport = new ValidationReport();
		
		InvalidConfigurationException.validateIsSet(id, "id");
		if(isJanusJar())
		{
			validationReport.noteFieldValue(true, "isJanusJar");
			InvalidConfigurationException.validateIsTrue(buildRootDirectory == null && exportDirectory == null,
					"buildRootDirectory and exportDirectory can't be set for janus jar");
		}
		else if(isJanusConfig())
		{
			validationReport.noteFieldValue(true, "isJanusConfig");
			InvalidConfigurationException.validateIsTrue(buildRootDirectory == null && exportDirectory == null,
					"buildRootDirectory and exportDirectory can't be set for janus config");
		}
		else
			InvalidConfigurationException.validateIsTrue(!(buildRootDirectory == null && exportDirectory == null), "either buildRootDirectory or exportDirectory has to be set");
		if(buildRootDirectory != null)
			InvalidConfigurationException.validateIsTrue(id.equals(getBuildRootDirectory().getName()), "the name of buildRootDirectory has to be the id of the project");
		validationReport.noteFieldValue(buildRootDirectory, "buildRootDirectory");
		validationReport.noteFieldValue(exportDirectory, "exportDirectory");
		validateComponents(validationReport);
		
		return validationReport.complete();
	}
	
	private void validateComponents(ValidationReport validationReport)
			throws InvalidConfigurationException
	{
		for(int i = 0; i < components.size(); i++)
		{
			var projectComponent = components.get(i);
			try
			{
				var componentValidationReport = projectComponent.validate();
				validationReport.addSubreport(componentValidationReport, "component "+projectComponent.getComponentId());
			}
			catch(InvalidConfigurationException e)
			{
				throw new InvalidConfigurationException("configuration error in projectComponent at index "+i, e);
			}
			
			projectDependencyFacade.validateComponentExists(projectComponent.getComponentId());
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
		return new File(buildRootDirectory);
	}
	
	public File getExportDirectory()
	{
		if(exportDirectory == null)
			return null;
		return new File(exportDirectory);
	}
	
	public boolean isJanusJar()
	{
		return "___janusJar".equals(id);
	}
	
	public boolean isJanusConfig()
	{
		return "___janusConfig".equals(id);
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
		public ValidationReport validate()
				throws InvalidConfigurationException
		{
			var validationReport = new ValidationReport();
			
			InvalidConfigurationException.validateIsSet(componentId, "componentId");
			validationReport.noteFieldValue(directoryInBuild, "directoryInBuild");
			
			return validationReport.complete();
		}
		
		
		// GETTERS
		public File getDirectoryInBuild(File buildDirectory)
		{
			if(directoryInBuild == null)
				return buildDirectory;
			return new File(buildDirectory, directoryInBuild);
		}
		
	}
	
}
