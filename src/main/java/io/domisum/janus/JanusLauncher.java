package io.domisum.janus;

import io.domisum.janus.dependencyinjection.JanusInjector;
import io.domisum.lib.auxiliumlib.util.java.thread.ThreadUtil;
import io.domisum.lib.auxiliumlib.util.java.thread.ThreadWatchdog;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JanusLauncher
{
	
	public static void main(String[] args)
	{
		ThreadUtil.logUncaughtExceptions(Thread.currentThread());
		
		var janus = JanusInjector.create().getInstance(Janus.class);
		
		ThreadWatchdog.registerOnTerminationAction(Thread.currentThread(), janus::stop);
		janus.start();
		ThreadWatchdog.unregisterOnTerminationActions(Thread.currentThread());
	}
	
}
