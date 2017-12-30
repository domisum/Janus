package de.domisum.janusinfinifrons.intercom;

import de.domisum.janusinfinifrons.intercom.responder.RequestResponder;
import de.domisum.janusinfinifrons.intercom.responder.RequestResponderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IntercomServer
{

	private final Logger logger = LoggerFactory.getLogger(getClass());


	// START STOP
	public abstract void start();

	public abstract void stop();


	// REQUESTS
	protected synchronized void handleRequest(ServerRequest request, ResponseSender responseSender)
	{
		logger.info("Received request: {}", request);

		RequestResponderFactory requestResponderFactory = new RequestResponderFactory(responseSender);
		RequestResponder requestResponder = requestResponderFactory.fromRequestPath(request);
		requestResponder.handleRequest();
	}

}
