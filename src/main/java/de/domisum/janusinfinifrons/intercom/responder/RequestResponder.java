package de.domisum.janusinfinifrons.intercom.responder;

import de.domisum.janusinfinifrons.intercom.ResponseSender;
import de.domisum.janusinfinifrons.intercom.ServerRequest;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class RequestResponder
{

	protected ServerRequest request;
	protected final ResponseSender responseSender;


	// REQUEST
	public abstract void handleRequest();

}
