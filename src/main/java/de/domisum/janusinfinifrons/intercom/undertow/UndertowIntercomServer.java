package de.domisum.janusinfinifrons.intercom.undertow;

import de.domisum.janusinfinifrons.intercom.IntercomServer;
import de.domisum.janusinfinifrons.intercom.ResponseSender;
import de.domisum.janusinfinifrons.intercom.ServerRequest;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.server.HttpServerExchange;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@RequiredArgsConstructor
public class UndertowIntercomServer extends IntercomServer
{

	private final Logger logger = LoggerFactory.getLogger(getClass());


	// SETTINGS
	private final int port;

	// SERVER
	private Undertow server;


	@Override public void start()
	{
		if(server != null)
			return;
		logger.info("Starting {}...", getClass().getSimpleName());

		Builder serverBuilder = Undertow.builder();
		serverBuilder.addHttpListener(port, "localhost", this::handleRequest);
		server = serverBuilder.build();

		server.start();
	}

	@Override public void stop()
	{
		logger.info("Stopping {}...", getClass().getSimpleName());
		server.stop();
	}


	// REQUEST
	private void handleRequest(HttpServerExchange exchange)
	{
		Map<String, List<String>> queryParams = new HashMap<>();
		for(Entry<String, Deque<String>> entry : exchange.getQueryParameters().entrySet())
			queryParams.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
		ServerRequest request = new ServerRequest(exchange.getRequestPath(), queryParams);

		ResponseSender responseSender = new UndertowResponseSender(exchange);
		handleRequest(request, responseSender);
	}

}
