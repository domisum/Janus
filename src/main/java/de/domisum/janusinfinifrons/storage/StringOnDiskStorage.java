package de.domisum.janusinfinifrons.storage;

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

@RequiredArgsConstructor
public class StringOnDiskStorage implements Storage<String>
{

	private Logger logger = LoggerFactory.getLogger(getClass());


	private final File directory;
	private final String fileExtension;


	// STORAGE
	@Override public String fetch(String id)
	{
		File file = new File(directory, id+"."+fileExtension);
		if(!file.exists())
			return null;

		return FileUtil.readFileToString(file);
	}

	@Override public Collection<String> fetchAll()
	{
		List<String> strings = new ArrayList<>();
		List<File> files = FileUtil.listFilesRecursively(directory, false);
		for(File f : files)
		{
			String extension = FilenameUtils.getExtension(f.getName());
			if(!Objects.equals(fileExtension, extension))
			{
				logger.warn("Storage directory contains file with invalid extension ({}), skipping: {}", fileExtension,
						f.getName());
				continue;
			}

			String string = FileUtil.readFileToString(f);
			strings.add(string);
		}

		return strings;
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
