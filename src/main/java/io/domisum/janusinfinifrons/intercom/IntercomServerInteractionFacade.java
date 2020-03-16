package io.domisum.janusinfinifrons.intercom;

import io.domisum.janusinfinifrons.build.ProjectBuild;

public interface IntercomServerInteractionFacade
{

	ProjectBuild getLatestBuild(String projectName);

}
