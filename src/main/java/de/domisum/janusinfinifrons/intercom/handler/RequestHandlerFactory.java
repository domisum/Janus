package de.domisum.janusinfinifrons.intercom.handler;

import de.domisum.janusinfinifrons.intercom.ResponseSender;
import de.domisum.janusinfinifrons.intercom.ServerRequest;
import de.domisum.janusinfinifrons.intercom.handler.handlers.UpdateAvailableRequestHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RequestHandlerFactory
{

	// INPUT
	private final ResponseSender responseSender;


	public RequestHandler fromRequestPath(ServerRequest serverRequest)
	{
		if("/updateAvailable".equalsIgnoreCase(serverRequest.getPath()))
			return new UpdateAvailableRequestHandler(serverRequest, responseSender);

		throw new IllegalArgumentException("invalid request: "+serverRequest);
	}

}
