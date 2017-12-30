package de.domisum.janusinfinifrons.intercom.undertow;

import de.domisum.janusinfinifrons.intercom.ResponseSender;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public class UndertowResponseSender implements ResponseSender
{

	private final HttpServerExchange undertowExchange;


	// SENDING
	@Override public void sendPlaintext(String text)
	{
		undertowExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
		undertowExchange.getResponseSender().send(text);
	}

	@Override public void sendJson(String json)
	{
		undertowExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
		undertowExchange.getResponseSender().send(json);
	}

	@Override public void sendRaw(byte[] data)
	{
		undertowExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/octet-stream");
		undertowExchange.getResponseSender().send(ByteBuffer.wrap(data));
	}

}
