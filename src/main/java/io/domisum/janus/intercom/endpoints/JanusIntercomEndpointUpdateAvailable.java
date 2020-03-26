package io.domisum.janus.intercom.endpoints;

import io.domisum.lib.httpbutler.HttpResponseSender;
import io.domisum.lib.httpbutler.endpointtypes.HttpButlerEndpointTypeStaticPath;
import io.domisum.lib.httpbutler.exceptions.HttpException;
import io.domisum.lib.httpbutler.request.HttpMethod;
import io.domisum.lib.httpbutler.request.HttpRequest;

public class JanusIntercomEndpointUpdateAvailable
		extends HttpButlerEndpointTypeStaticPath
{
	
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
			throws HttpException
	{
		// TODO
	}
	
}
