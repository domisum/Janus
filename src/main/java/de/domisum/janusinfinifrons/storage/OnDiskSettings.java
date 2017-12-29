package de.domisum.janusinfinifrons.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
public class OnDiskSettings
{

	@Getter private final File directory;
	@Getter private final String fileExtension;

}
