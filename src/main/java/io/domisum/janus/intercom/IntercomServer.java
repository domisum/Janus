package io.domisum.janus.intercom;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.domisum.lib.httpbutler.HttpButlerEndpoint;
import io.domisum.lib.httpbutler.HttpButlerServer;

import java.util.Set;

@Singleton
public class IntercomServer
		extends HttpButlerServer
{
	
	// CONSTANTS
	private static final int PORT = 8381;
	
	
	// INIT
	@Inject
	public IntercomServer(Set<HttpButlerEndpoint> endpoints)
	{
		super("localhost", PORT, endpoints);
	}
	
}
