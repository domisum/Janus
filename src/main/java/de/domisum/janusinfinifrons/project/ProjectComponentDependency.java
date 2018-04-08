package de.domisum.janusinfinifrons.project;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProjectComponentDependency
{

	@Getter private final String componentId;
	private final String inBuildPath;


	// GETTERS
	public String getInBuildPath()
	{
		if(inBuildPath == null)
			return "/";

		return inBuildPath;
	}

}
