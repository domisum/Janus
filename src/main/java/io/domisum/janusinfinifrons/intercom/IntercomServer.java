package io.domisum.janusinfinifrons.intercom;

import io.domisum.janusinfinifrons.intercom.handlers.UpdateAvailableRequestHandler;
import io.domisum.lib.httpbutler.HttpButlerServer;
import io.domisum.lib.httpbutler.request.HttpMethod;

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
