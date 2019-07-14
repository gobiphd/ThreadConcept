package com.gobi.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public final class QueueSemaphore {

	private final ExecutorService executor;
	private final Semaphore semaphore;

	public QueueSemaphore(ExecutorService executor, int limit) {
		this.executor = executor;
		this.semaphore = new Semaphore(limit);
	}

	public <T> Future<T> submit(final Callable<T> task) throws InterruptedException {

		semaphore.acquire();
		System.out.println("semaphore.acquire()...");

		return executor.submit(() -> {
			try {
				return task.call();
			} finally {
				semaphore.release();
				System.out.println("semaphore.release()...");
			}
		});
	}
}
