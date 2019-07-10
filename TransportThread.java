
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class TransportThread extends Thread {

	@Override
	public void run() {
		System.out.println("Transport Started!");
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
			System.out.println("Transport Completed!");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			process.destroy();
		}
	}
}
