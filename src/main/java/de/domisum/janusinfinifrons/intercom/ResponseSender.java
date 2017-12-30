package de.domisum.janusinfinifrons.intercom;

public interface ResponseSender
{

	void sendPlaintext(String text);

	void sendJson(String json);

	void sendRaw(byte[] data);

}
