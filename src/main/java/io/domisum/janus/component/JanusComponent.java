package io.domisum.janus.component;

import io.domisum.janus.JanusConfigObject;
import io.domisum.janus.ValidationReport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
public abstract class JanusComponent implements JanusConfigObject
{
	
	// ATTRIBUTES
	@Getter
	private final String id;
	@Getter
	private final String credentialId;
	
	
	// INIT
	@Override
	public ValidationReport validate()
	{
		var validationReport = new ValidationReport();
		
		Validate.notNull(id, "id can't be null");
		validationReport.noteFieldValue(credentialId, "credentialId");
		validateTypeSpecific(validationReport);
		
		return validationReport.complete();
	}
	
	protected abstract void validateTypeSpecific(ValidationReport validationReport);
	
	
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
	
	public abstract void addToBuild(File inBuildDir);
	
	
	// UTIL
	protected File getDirectory()
	{
		return new File("_components/"+getId());
	}
	
}
