package com.gobi.thread;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ExtractThread extends Thread {

	private String strExtractThreadCount;

	private String command;

	private String userId;

	private String datasetId;

	private List<Integer> mappingList;

	public ExtractThread(String strExtractThreadCount, List<Integer> mappingList, String command, String userId, String datasetId) {
		this.strExtractThreadCount = strExtractThreadCount;
		this.mappingList = mappingList;
		this.command = command;
		this.userId = userId;
		this.datasetId = datasetId;
	}

	@Override
	public void run() {
		System.out.println("ExtractThread Started!");
		int extractThreadCount = 0;
		String extKey = "";
		String transKey = "";
		String completedMappingPath = userId+"/"+datasetId;
		List<Future<Map<String, Object>>> list = new CopyOnWriteArrayList<>();
		Future<Map<String, Object>> future = null;
		ExecutorService executor = null;
		QueueSemaphore queueSemaphore = null;
		try {
			executor = Executors.newCachedThreadPool();

			if (strExtractThreadCount != null && !strExtractThreadCount.isEmpty()) {
				extractThreadCount = Integer.parseInt(strExtractThreadCount);
			}

			if (executor != null) {
				queueSemaphore = new QueueSemaphore(executor, extractThreadCount);
			}

			if (queueSemaphore != null) {

				if (mappingList != null && mappingList.size() > 0) {

					for (Integer newMappingId : mappingList) {
						try {
							future = queueSemaphore.submit(new ExtractMappingThread(newMappingId, command));

							list.add(future);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				if (list != null && list.size() > 0) {
					for (Future<Map<String, Object>> futureMap : list) {
						try {
							Map<String, Object> returnMap = futureMap.get();

							if (returnMap != null && returnMap.size() > 0) {
								String status = (String)returnMap.get("STATUS");
								int mappingId = (int)returnMap.get("MAPPING_ID");

								extKey = "EXT_"+mappingId;
								transKey = "TRANS_"+mappingId;

								String extFilePath = FileUtils.getFilePath(completedMappingPath, extKey, status);
								FileUtils.touch(extFilePath);

								if ("FAIL".equals(status)) {
									String transFilePath = FileUtils.getFilePath(completedMappingPath, transKey, status);
									FileUtils.touch(transFilePath);
								}
								list.remove(futureMap);
								System.out.println("Extract :: "+extKey+" ==> "+status);
							}
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
					}
				}

				executor.shutdown();
				while (!executor.isTerminated()) {
				}
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		System.out.println("ExtractThread Completed!");
	}
}
