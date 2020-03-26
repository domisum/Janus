package io.domisum.janus.project;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
public class JanusProjectBuild
{

	@Getter
	private final File directory;

}
