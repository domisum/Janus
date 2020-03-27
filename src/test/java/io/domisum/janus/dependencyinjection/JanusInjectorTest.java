package io.domisum.janus.dependencyinjection;

import io.domisum.janus.Janus;
import org.junit.jupiter.api.Test;

public class JanusInjectorTest
{
	
	@Test
	public void testCreate()
	{
		JanusInjector.create().getInstance(Janus.class);
	}
	
}
