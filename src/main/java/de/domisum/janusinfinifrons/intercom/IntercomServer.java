package de.domisum.janusinfinifrons.intercom;

import de.domisum.httpbutler.HttpButlerServer;
import de.domisum.httpbutler.request.HttpMethod;
import de.domisum.janusinfinifrons.intercom.handlers.UpdateAvailableRequestHandler;

public class IntercomServer extends HttpButlerServer
{

	// INIT
	public IntercomServer(int port, IntercomServerInteractionFacade facade)
	{
		super("localhost", port);

		setNumberOfIoThreads(1);
		setNumberOfWorkerThreads(1);

		registerStaticPathRequestHandler(HttpMethod.GET, "/updateAvailable", new UpdateAvailableRequestHandler(facade));
	}

}
