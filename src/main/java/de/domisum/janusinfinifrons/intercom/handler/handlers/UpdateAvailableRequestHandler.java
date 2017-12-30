package de.domisum.janusinfinifrons.intercom.handler.handlers;

import de.domisum.janusinfinifrons.build.ProjectBuild;
import de.domisum.janusinfinifrons.intercom.IntercomServerInteractionFacade;
import de.domisum.janusinfinifrons.intercom.ResponseSender;
import de.domisum.janusinfinifrons.intercom.ServerRequest;
import de.domisum.janusinfinifrons.intercom.handler.RequestHandler;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class UpdateAvailableRequestHandler extends RequestHandler
{

	// INIT
	public UpdateAvailableRequestHandler(
			IntercomServerInteractionFacade interactionFacade, ServerRequest request, ResponseSender responseSender)
	{
		super(interactionFacade, request, responseSender);
	}


	// RESPONDER
	@Override public void handleRequest()
	{
		File directory = new File(validateAndGetDirectoryParam());

		String buildName = directory.getName();
		String projectName = getProjectNameFromBuildName(buildName);

		ProjectBuild latestBuild = getInteractionFacade().getLatestBuild(projectName);
		boolean isNewerBuildAvailable = !Objects.equals(buildName, latestBuild.getBuildId());

		getResponseSender().sendPlaintext(isNewerBuildAvailable+"");
	}

	private String validateAndGetDirectoryParam()
	{
		List<String> directoryParam = getRequest().getQueryParameters().get("directory");
		if(directoryParam == null)
			throw new IllegalArgumentException("missing parameter 'directory'");

		if(directoryParam.size() != 1)
			throw new IllegalArgumentException("wrong cardinality of parameter directory: need 1, but got"+directoryParam.size());

		return directoryParam.get(0);
	}


	private String getProjectNameFromBuildName(String buildName)
	{
		int lastIndex = buildName.lastIndexOf('#');

		return buildName.substring(0, lastIndex);
	}

}
