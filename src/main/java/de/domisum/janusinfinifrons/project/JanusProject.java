package de.domisum.janusinfinifrons.project;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class JanusProject
{

	@Getter private final String id;
	@Getter private final List<String> componentIds;

}
