package de.domisum.janusinfinifrons.intercom;

import de.domisum.janusinfinifrons.build.ProjectBuild;

public interface IntercomServerInteractionFacade
{

	ProjectBuild getLatestBuild(String projectName);

}
