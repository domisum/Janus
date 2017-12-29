package de.domisum.janusinfinifrons.storage;

import de.domisum.lib.auxilium.contracts.storage.Storage;
import de.domisum.lib.auxilium.util.FileUtil;
import de.domisum.lib.auxilium.util.FileUtil.FileType;
import lombok.RequiredArgsConstructor;
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
	private final File directory;
	private final String fileExtension;


	// STORAGE
	@Override public Optional<String> fetch(String id)
	{
		File file = new File(directory, id+getFileExtension());
		if(!file.exists())
			return Optional.empty();

		return Optional.of(FileUtil.readString(file));
	}

	@Override public Collection<String> fetchAll()
	{
		List<String> strings = new ArrayList<>();
		Collection<File> files = FileUtil.listFiles(directory, FileType.FILE);
		for(File f : files)
			loadFile(f).ifPresent(strings::add);

		return strings;
	}

	private Optional<String> loadFile(File file)
	{
		String extension = FileUtil.getExtendedFileExtension(file);
		if(!Objects.equals(getFileExtension(), extension))
		{
			logger.warn("Storage directory contains file with invalid extension ({}), skipping: {}", extension, file.getName());
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


	// UTIL
	private String getFileExtension()
	{
		if(fileExtension.startsWith("."))
			return fileExtension;

		return "."+fileExtension;
	}

}
