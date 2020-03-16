package io.domisum.janusinfinifrons.project;

import io.domisum.janusinfinifrons.component.JanusComponent;
import io.domisum.lib.auxiliumlib.contracts.Identifyable;
import io.domisum.lib.auxiliumlib.contracts.source.optional.FiniteOptionalSource;
import io.domisum.lib.auxiliumlib.util.PHR;
import io.domisum.lib.auxiliumlib.util.java.exceptions.InvalidConfigurationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

import java.util.List;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class JanusProject implements Identifyable
{

	@Getter private final String id;
	@Getter private final List<ProjectComponentDependency> componentDependencies;


	public void validate(FiniteOptionalSource<String, JanusComponent> componentSource)
	{
		Validate.notNull(componentDependencies, "componentDependencies can't be null");

		for(ProjectComponentDependency componentDependency : componentDependencies)
		{
			String cid = componentDependency.getComponentId();
			if(!componentSource.contains(cid))
				throw new InvalidConfigurationException(PHR.r("project '{}' depends on non-existant component '{}'", id, cid));
		}
	}

}
