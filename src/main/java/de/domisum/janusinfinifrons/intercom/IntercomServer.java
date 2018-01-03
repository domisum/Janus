package de.domisum.janusinfinifrons.intercom;

import de.domisum.janusinfinifrons.intercom.handler.RequestHandler;
import de.domisum.janusinfinifrons.intercom.handler.handlers.UpdateAvailableRequestHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public abstract class IntercomServer // TODO refactor to use HttpButler
{

	private final Logger logger = LoggerFactory.getLogger(getClass());


	// REFERENCES
	private final IntercomServerInteractionFacade interactionFacade;


	// START STOP
	public abstract void start();

	public abstract void stop();


	// REQUESTS
	protected synchronized void handleRequest(ServerRequest request, ResponseSender responseSender)
	{
		logger.debug("Received request: {}", request);

		constructHandler(request, responseSender).handleRequest();
	}

	private RequestHandler constructHandler(ServerRequest serverRequest, ResponseSender responseSender)
	{
		if("/updateAvailable".equalsIgnoreCase(serverRequest.getPath()))
			return new UpdateAvailableRequestHandler(interactionFacade, serverRequest, responseSender);

		throw new IllegalArgumentException("invalid request: "+serverRequest);
	}

}
