package io.domisum.janus.intercom;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.domisum.janus.api.JanusApiHttp;
import io.domisum.lib.httpbutler.HttpButlerServer;
import io.domisum.lib.httpbutler.HttpButlerEndpoint;

import java.util.Set;

@Singleton
public class JanusIntercomServer
		extends HttpButlerServer
{
	
	// INIT
	@Inject
	public JanusIntercomServer(Set<HttpButlerEndpoint> endpoints)
	{
		super("localhost", JanusApiHttp.PORT, endpoints);
	}
	
}
