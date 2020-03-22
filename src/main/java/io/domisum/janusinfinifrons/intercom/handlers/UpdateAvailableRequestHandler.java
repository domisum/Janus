package io.domisum.janusinfinifrons.intercom.handlers;

import io.domisum.lib.httpbutler.HttpRequestHandler;
import io.domisum.lib.httpbutler.HttpResponseSender;
import io.domisum.lib.httpbutler.exceptions.BadRequestHttpException;
import io.domisum.lib.httpbutler.request.HttpRequest;
import io.domisum.janusinfinifrons.intercom.IntercomServerInteractionFacade;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Objects;

@RequiredArgsConstructor
public class UpdateAvailableRequestHandler extends HttpRequestHandler
{

	// DEPENDENCIES
	private final IntercomServerInteractionFacade intercomServerInteractionFacade;


	// REQUEST
	@Override
	protected void handleRequest(HttpRequest request, HttpResponseSender responseSender) throws BadRequestHttpException
	{
		String directoryPath = request.getParameterValueOrException("directory");
		var directory = new File(directoryPath);

		String buildName = directory.getName();
		String projectName = getProjectNameFromBuildName(buildName);

		var latestBuild = intercomServerInteractionFacade.getLatestBuild(projectName);
		if(latestBuild == null)
		{
			responseSender.sendPlaintext("false");
			return;
		}

		boolean isNewerBuildAvailable = !Objects.equals(buildName, latestBuild.getBuildId());
		responseSender.sendPlaintext(isNewerBuildAvailable+"");
	}

	private String getProjectNameFromBuildName(String buildName) throws BadRequestHttpException
	{
		int lastIndex = buildName.lastIndexOf('#');
		if(lastIndex == -1)
			throw new BadRequestHttpException("failed to parse project name from directory");

		return buildName.substring(0, lastIndex);
	}

}
