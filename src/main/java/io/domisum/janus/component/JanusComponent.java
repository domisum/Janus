package io.domisum.janus.component;

import io.domisum.janus.ValidationReport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
public abstract class JanusComponent
{
	
	// ATTRIBUTES
	@Getter
	private final String id;
	@Getter
	private final String credentialId;
	
	
	// OBJECT
	@Override
	public String toString()
	{
		return getClass().getSimpleName()+"("+getId()+": "+getToStringInfos()+")";
	}
	
	protected abstract String getToStringInfos();
	
	
	// COMPONENT
	public ValidationReport validate()
	{
		var validationReport = new ValidationReport();
		
		Validate.notNull(id, "id can't be null");
		validationReport.noteFieldValue(credentialId, "credentialId");
		validateTypeSpecific(validationReport);
		
		validationReport.complete();
		return validationReport;
	}
	
	protected abstract void validateTypeSpecific(ValidationReport validationReport);
	
	public abstract boolean update()
			throws IOException;
	
	public abstract void addToBuild(File inBuildDir);
	
	
	// UTIL
	protected File getDirectory()
	{
		return new File("_components/"+getId());
	}
	
}
