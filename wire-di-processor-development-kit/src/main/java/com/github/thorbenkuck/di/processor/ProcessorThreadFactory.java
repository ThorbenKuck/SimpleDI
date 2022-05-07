package com.github.thorbenkuck.di.processor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class ProcessorThreadFactory implements ThreadFactory {

	private final AtomicInteger atomicInteger = new AtomicInteger(0);
	private final ThreadGroup group;
	private final ClassLoader classLoader;

	ProcessorThreadFactory() {
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup()
				: Thread.currentThread().getThreadGroup();
		classLoader = Thread.currentThread().getContextClassLoader();
	}

	@Override
	public Thread newThread(@NotNull Runnable r) {
		Thread thread = new Thread(group, r, "Processor-" + atomicInteger.incrementAndGet());
		thread.setContextClassLoader(classLoader);
		return thread;
	}
}