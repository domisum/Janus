package io.domisum.janus.dependencyinjection;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JanusInjector
{
	
	// GET
	public static Injector create()
	{
		return Guice.createInjector(new JanusGuiceModule());
	}
	
}
