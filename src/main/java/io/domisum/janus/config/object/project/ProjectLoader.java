package io.domisum.janus.config.object.project;

import com.google.inject.Inject;
import io.domisum.janus.config.object.ConfigObjectLoader;
import io.domisum.lib.auxiliumlib.util.GsonUtil;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProjectLoader
		extends ConfigObjectLoader<Project>
{
	
	// DEPENDENCIES
	private final ProjectDependencyFacade projectDependencyFacade;
	
	
	// CONSTANT METHODS
	@Override
	protected String OBJECT_NAME()
	{
		return "project";
	}
	
	@Override
	protected Project deserialize(String configContent)
	{
		return GsonUtil.get().fromJson(configContent, Project.class);
	}
	
	@Override
	protected Map<Class<?>,Object> getDependenciesToInject()
	{
		return Map.of(ProjectDependencyFacade.class, projectDependencyFacade);
	}
	
}
