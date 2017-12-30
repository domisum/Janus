package de.domisum.janusinfinifrons.intercom;

import de.domisum.janusinfinifrons.intercom.handler.RequestHandler;
import de.domisum.janusinfinifrons.intercom.handler.RequestHandlerFactory;
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

		RequestHandlerFactory requestHandlerFactory = new RequestHandlerFactory(responseSender);
		RequestHandler requestHandler = requestHandlerFactory.fromRequestPath(request);
		requestHandler.handleRequest();
	}

}
