

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ExtractThread extends Thread {

	@Override
	public void run() {
		System.out.println("ExtractThread Started!");

		ExecutorService executor = Executors.newCachedThreadPool();

		TaskLimitSemaphore obj = new TaskLimitSemaphore(executor, 1);

		for (int i = 0; i < 3; i++) {
			try {
				obj.submit(new ExtractMappingThread(i));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		executor.shutdown();
		while (!executor.isTerminated()) {
		}
		System.out.println("ExtractThread Completed!");
	}
}
