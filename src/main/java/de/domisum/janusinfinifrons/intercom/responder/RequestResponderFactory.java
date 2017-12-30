package de.domisum.janusinfinifrons.intercom.responder;

import de.domisum.janusinfinifrons.intercom.ResponseSender;
import de.domisum.janusinfinifrons.intercom.ServerRequest;
import de.domisum.janusinfinifrons.intercom.responder.responders.UpdateAvailableRequestResponder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RequestResponderFactory
{

	// INPUT
	private final ResponseSender responseSender;


	public RequestResponder fromRequestPath(ServerRequest serverRequest)
	{
		if("/updateAvailable".equalsIgnoreCase(serverRequest.getPath()))
			return new UpdateAvailableRequestResponder(serverRequest, responseSender);

		throw new IllegalArgumentException("invalid request: "+serverRequest);
	}

}
