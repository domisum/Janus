package de.domisum.janusinfinifrons.intercom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@ToString
public class ServerRequest
{

	@Getter private final String path;
	@Getter private final Map<String, List<String>> queryParameters;

}
