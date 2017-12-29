package de.domisum.janusinfinifrons.project;

import de.domisum.janusinfinifrons.component.JanusComponent;
import de.domisum.lib.auxilium.contracts.Identifyable;
import de.domisum.lib.auxilium.contracts.source.FiniteSource;
import de.domisum.lib.auxilium.util.PHR;
import de.domisum.lib.auxilium.util.java.exceptions.InvalidConfigurationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class JanusProject implements Identifyable
{

	@Getter private final String id;
	@Getter private final List<String> componentIds;


	public void validate(FiniteSource<String, JanusComponent> componentSource)
	{
		for(String cid : componentIds)
			if(!componentSource.contains(cid))
				throw new InvalidConfigurationException(PHR.r("project '{}' depends on non-existant component '{}'", id, cid));
	}

}
