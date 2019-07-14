package com.gobi.thread;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public final class LoadingMappingThread  implements Callable<Map<String, Object>> {

	private String command;

	private int mappingId;

	public LoadingMappingThread(int mappingId, String command) {
		this.mappingId = mappingId;
		this.command = command;
	}

	@Override
	public Map<String, Object> call() throws Exception {
		System.out.println("LoadingMappingThread Started! :: Mapping Id : "+ mappingId);
		String processStatus = "";
		Map<String, Object> returnMap = new HashMap<>();
		ProcessBuilder pb = null;
		Process process = null;
		IOThreadHandler outputHandler = null;
		try {
			returnMap.put("MAPPING_ID", mappingId);

			if (command != null && !command.isEmpty()) {
				pb = new ProcessBuilder(command.split(" "));
				process = pb.start();
				outputHandler = new IOThreadHandler(process.getInputStream());
				outputHandler.start();
				process.waitFor();
				System.out.println(outputHandler.getOutput());
				processStatus = "SUCCESS";
				returnMap.put("STATUS", processStatus);
			}

			System.out.println("LoadingMappingThread Completed!:: Mapping Id : "+ mappingId);
		} catch (IOException | InterruptedException e) {
			processStatus = "FAIL";
			returnMap.put("STATUS", processStatus);
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
		return returnMap;
	}
}
