package io.domisum.janusinfinifrons.build;

import io.domisum.janusinfinifrons.instance.JanusProjectInstance;
import io.domisum.janusinfinifrons.project.JanusProject;
import io.domisum.lib.auxiliumlib.contracts.Keyable;
import io.domisum.lib.auxiliumlib.util.file.FileUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@RequiredArgsConstructor
@EqualsAndHashCode(of = {"project", "buildId"})
public class ProjectBuild implements Keyable<JanusProject>
{

	private final transient Logger logger = LoggerFactory.getLogger(getClass());


	// ATTRIBUTES
	@Getter private final JanusProject project;
	@Getter private final String buildId;

	@Getter private final File directory;


	// OBJECT
	@Override public String toString()
	{
		return buildId;
	}


	// STORAGE
	@Override public JanusProject getKey()
	{
		return project;
	}


	// FILES
	public void delete()
	{
		FileUtil.deleteDirectory(directory);
	}

	public void exportTo(JanusProjectInstance instance)
	{
		logger.info("Exporting build '{}' to instance '{}'..", this, instance.getId());

		FileUtil.copyDirectory(directory, new File(instance.getRootDirectory(), buildId));
		instance.writeLatestBuildId(buildId);
	}

}
