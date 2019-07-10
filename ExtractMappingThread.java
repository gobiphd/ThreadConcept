

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ExtractMappingThread implements Callable<Integer> {

	private int threadNo;

	public ExtractMappingThread(int threadNo) {
		this.threadNo = threadNo;
	}

	@Override
	public Integer call() throws Exception {
		System.out.println("ExtractMappingThread Started! :: Thread No : "+ threadNo);
		List<String> cmdList = new ArrayList<>();
		Process process = null;
		try {
			cmdList.add("java");
			cmdList.add("-jar");
			cmdList.add("/home/gobinath/Desktop/TempJar.jar");

			ProcessBuilder pb = new ProcessBuilder(cmdList);
			process = pb.start();
			IOThreadHandler outputHandler = new IOThreadHandler(process.getInputStream());
			outputHandler.start();
			process.waitFor();
			System.out.println(outputHandler.getOutput());
			System.out.println("ExtractMappingThread Completed!:: Thread No : "+ threadNo);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			process.destroy();
		}
		return threadNo;
	}
}
