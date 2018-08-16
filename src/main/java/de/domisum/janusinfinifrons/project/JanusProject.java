package de.domisum.janusinfinifrons.project;

import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.lib.auxilium.contracts.Identifyable;
import de.domisum.lib.auxilium.contracts.source.optional.FiniteOptionalSource;
import de.domisum.lib.auxilium.util.PHR;
import de.domisum.lib.auxilium.util.java.exceptions.InvalidConfigurationException;
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
