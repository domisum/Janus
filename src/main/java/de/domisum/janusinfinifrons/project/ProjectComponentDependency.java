package de.domisum.janusinfinifrons.project;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProjectComponentDependency
{

	@Getter private final String componentId;
	@Getter private final String inBuildPath = "/";

}
