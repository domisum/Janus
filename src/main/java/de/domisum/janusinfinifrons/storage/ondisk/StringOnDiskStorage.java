package de.domisum.janusinfinifrons.storage.ondisk;

import de.domisum.lib.auxilium.contracts.storage.Storage;
import de.domisum.lib.auxilium.util.FileUtil;
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

	private Logger logger = LoggerFactory.getLogger(getClass());


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
		Collection<File> files = FileUtil.getDirectoryContents(onDiskSettings.getDirectory());
		for(File f : files)
		{
			String fileContent = loadFile(f);

			if(fileContent != null)
				strings.add(fileContent);
		}

		return strings;
	}

	private String loadFile(File file)
	{
		String extension = FilenameUtils.getExtension(file.getName());
		if(!Objects.equals(onDiskSettings.getFileExtension(), extension))
		{
			logger.warn(
					"Storage directory contains file with invalid extension ({}), skipping: {}",
					onDiskSettings.getFileExtension(),
					file.getName());
			return null;
		}

		return FileUtil.readString(file);
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
