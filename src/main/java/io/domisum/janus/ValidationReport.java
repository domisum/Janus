package io.domisum.janus;

import io.domisum.lib.auxiliumlib.PHR;
import io.domisum.lib.auxiliumlib.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ValidationReport
{
	
	// CONSTANTS
	private static final String TO_STRING_DELIMITER = ";";
	
	// ATTRIBUTES
	private final List<String> lines = new ArrayList<>();
	private boolean completed = false;
	
	
	// OBJECT
	@Override
	public String toString()
	{
		return StringUtil.listToString(lines, TO_STRING_DELIMITER);
	}
	
	
	// REPORT
	public void append(String line, Object... placeholderValues)
	{
		// throw errors so catch around validation does not catch these
		if(completed)
			throw new Error("can't append to validation report when it is already completed");
		if(line.contains(TO_STRING_DELIMITER))
			throw new Error("line is not allowed to contain validation report delimiter '"+TO_STRING_DELIMITER+"'");
		
		lines.add(PHR.r(line, placeholderValues));
	}
	
	public void noteFieldValue(Object value, String fieldName)
	{
		String contentDisplay = value == null ? "(not set)" : "'"+value.toString()+"'";
		append("field {} is set to value {}", fieldName, contentDisplay);
	}
	
	public void complete()
	{
		if(completed)
			throw new Error("can't complete when already completed");
		completed = true;
	}
	
}
