package de.domisum.janusinfinifrons.intercom.handlers;

import de.domisum.httpbutler.HttpRequestHandler;
import de.domisum.httpbutler.HttpResponseSender;
import de.domisum.httpbutler.exceptions.BadRequestHttpException;
import de.domisum.httpbutler.request.HttpRequest;
import de.domisum.janusinfinifrons.intercom.IntercomServerInteractionFacade;
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
		String directoryPath = request.getParameterValueOrError("directory");
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
