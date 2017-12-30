package de.domisum.janusinfinifrons.intercom.responder.responders;

import de.domisum.janusinfinifrons.intercom.ResponseSender;
import de.domisum.janusinfinifrons.intercom.ServerRequest;
import de.domisum.janusinfinifrons.intercom.responder.RequestResponder;

public class UpdateAvailableRequestResponder extends RequestResponder
{

	// INIT
	public UpdateAvailableRequestResponder(ServerRequest request, ResponseSender responseSender)
	{
		super(request, responseSender);
	}

	// RESPONDER
	@Override public void handleRequest()
	{
		responseSender.sendPlaintext("wow");
	}

}
