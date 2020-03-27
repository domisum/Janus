package io.domisum.janus.config.object.project;

import io.domisum.janus.config.object.ConfigObject;
import io.domisum.janus.config.object.ValidationReport;
import io.domisum.lib.auxiliumlib.exceptions.InvalidConfigurationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.util.List;

@RequiredArgsConstructor
public class Project
		implements ConfigObject
{
	
	// ATTRIBUTES
	@Getter
	private final String id;
	
	@Getter
	private final String buildRootDirectory;
	@Getter
	private final String exportDirectory;
	
	// COMPONENTS
	private final List<ProjectComponent> components;
	
	
	// INIT
	@Override
	public ValidationReport validate()
	{
		var validationReport = new ValidationReport();
		
		Validate.notNull(id, "id has to be set");
		Validate.isTrue(!(buildRootDirectory == null && exportDirectory == null), "either buildRootDirectory or exportDirectory has to be set");
		validationReport.noteFieldValue(buildRootDirectory, "buildRootDirectory");
		validationReport.noteFieldValue(exportDirectory, "exportDirectory");
		validateComponents(validationReport);
		
		return validationReport.complete();
	}
	
	private void validateComponents(ValidationReport validationReport)
	{
		for(int i = 0; i < components.size(); i++)
		{
			var component = components.get(i);
			try
			{
				var componentValidationReport = component.validate();
				validationReport.addSubreport(componentValidationReport, component.getComponentId());
			}
			catch(RuntimeException e)
			{
				throw new InvalidConfigurationException("configuration error in component at index "+i, e);
			}
		}
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
		{
			var validationReport = new ValidationReport();
			
			Validate.notNull(componentId, "componentId can't be null");
			validationReport.noteFieldValue(directoryInBuild, "directoryInBuild");
			
			return validationReport.complete();
		}
		
	}
	
}
