package de.domisum.janusinfinifrons.storage.ondisk;

import de.domisum.lib.auxilium.contracts.storage.Storage;
import de.domisum.lib.auxilium.util.FileUtil;
import de.domisum.lib.auxilium.util.FileUtil.FileType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class StringOnDiskStorage implements Storage<String, String>
{

	private final Logger logger = LoggerFactory.getLogger(getClass());


	// SETTINGS
	private final OnDiskSettings onDiskSettings;


	// STORAGE
	@Override public Optional<String> fetch(String id)
	{
		File file = new File(onDiskSettings.getDirectory(), id+"."+onDiskSettings.getFileExtension());
		if(!file.exists())
			return Optional.empty();

		return Optional.of(FileUtil.readString(file));
	}

	@Override public Collection<String> fetchAll()
	{
		List<String> strings = new ArrayList<>();
		Collection<File> files = FileUtil.listFiles(onDiskSettings.getDirectory(), FileType.FILE);
		for(File f : files)
			loadFile(f).ifPresent(strings::add);

		return strings;
	}

	private Optional<String> loadFile(File file)
	{
		String extension = FilenameUtils.getExtension(file.getName());
		if(!Objects.equals(onDiskSettings.getFileExtension(), extension))
		{
			logger.warn(
					"Storage directory contains file with invalid extension ({}), skipping: {}",
					onDiskSettings.getFileExtension(),
					file.getName());
			return Optional.empty();
		}

		return Optional.of(FileUtil.readString(file));
	}


	@Override public void store(String item)
	{
		throw new UnsupportedOperationException();
	}

	@Override public boolean contains(String id)
	{
		throw new UnsupportedOperationException();
	}

	@Override public void remove(String id)
	{
		throw new UnsupportedOperationException();
	}

}
