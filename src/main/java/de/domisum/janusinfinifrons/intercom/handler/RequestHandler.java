package de.domisum.janusinfinifrons.intercom.handler;

import de.domisum.janusinfinifrons.intercom.IntercomServerInteractionFacade;
import de.domisum.janusinfinifrons.intercom.ResponseSender;
import de.domisum.janusinfinifrons.intercom.ServerRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class RequestHandler
{

	@Getter(AccessLevel.PROTECTED) private final IntercomServerInteractionFacade interactionFacade;

	@Getter(AccessLevel.PROTECTED) private final ServerRequest request;
	@Getter(AccessLevel.PROTECTED) private final ResponseSender responseSender;


	// REQUEST
	public abstract void handleRequest();

}
