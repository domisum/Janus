package de.domisum.janusinfinifrons.intercom.handler.handlers;

import de.domisum.janusinfinifrons.intercom.ResponseSender;
import de.domisum.janusinfinifrons.intercom.ServerRequest;
import de.domisum.janusinfinifrons.intercom.handler.RequestHandler;

public class UpdateAvailableRequestHandler extends RequestHandler
{

	// INIT
	public UpdateAvailableRequestHandler(ServerRequest request, ResponseSender responseSender)
	{
		super(request, responseSender);
	}


	// RESPONDER
	@Override public void handleRequest()
	{
		responseSender.sendPlaintext("wow");
	}

}
