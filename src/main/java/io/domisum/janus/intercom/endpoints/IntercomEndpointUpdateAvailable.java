package io.domisum.janus.intercom.endpoints;

import com.google.inject.Inject;
import io.domisum.janus.build.LatestBuildRegistry;
import io.domisum.lib.httpbutler.HttpResponseSender;
import io.domisum.lib.httpbutler.endpointtypes.HttpButlerEndpointTypeStaticPath;
import io.domisum.lib.httpbutler.exceptions.BadRequestHttpException;
import io.domisum.lib.httpbutler.request.HttpMethod;
import io.domisum.lib.httpbutler.request.HttpRequest;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IntercomEndpointUpdateAvailable
		extends HttpButlerEndpointTypeStaticPath
{
	
	// DEPENDENCIES
	private final LatestBuildRegistry latestBuildRegistry;
	
	
	// CONSTANTS
	@Override
	protected HttpMethod METHOD()
	{
		return HttpMethod.GET;
	}
	
	@Override
	protected String PATH()
	{
		return "/updateAvailable";
	}
	
	
	// HANDLING
	@Override
	protected void handleRequest(HttpRequest request, HttpResponseSender responseSender)
			throws BadRequestHttpException
	{
		String projectId = request.getQueryParameterValue("project");
		String buildName = request.getQueryParameterValue("build");
		
		var latestBuildOptional = latestBuildRegistry.get(projectId);
		if(latestBuildOptional.isEmpty())
		{
			responseSender.sendPlaintext("false");
			return;
		}
		
		String latestBuild = latestBuildOptional.get();
		boolean updateAvailable = !Objects.equals(latestBuild, buildName);
		responseSender.sendPlaintext(updateAvailable+"");
	}
	
}
