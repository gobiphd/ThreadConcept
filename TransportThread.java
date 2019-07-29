package com.gobi.thread;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class TransportThread extends Thread {

	private String strTransportThreadCount;

	private String command;

	private String userId;

	private String datasetId;

	private List<Integer> mappingList;

	public TransportThread(String strTransportThreadCount, List<Integer> mappingList, String command, String userId, String datasetId) {
		this.strTransportThreadCount = strTransportThreadCount;
		this.mappingList = mappingList;
		this.command = command;
		this.userId = userId;
		this.datasetId = datasetId;
	}

	@Override
	public void run() {
		System.out.println("TransportThread Started!");
		int transportThreadCount = 0;
		int iterationCount = 0;
		int mappingListSize = 0;
		String transKey = "";
		String loadKey = "";
		String completedMappingPath = userId+"/"+datasetId;
		List<Future<Map<String, Object>>> list = new CopyOnWriteArrayList<>();
		Map<String, String> completedMappingMap = null;
		Future<Map<String, Object>> future = null;
		ExecutorService executor = null;
		QueueSemaphore queueSemaphore = null;
		try {
			executor = Executors.newCachedThreadPool();

			if (strTransportThreadCount != null && !strTransportThreadCount.isEmpty()) {
				transportThreadCount = Integer.parseInt(strTransportThreadCount);
			}

			if (executor != null) {
				queueSemaphore = new QueueSemaphore(executor, transportThreadCount);
			}

			if (queueSemaphore != null) {

				if (mappingList != null && mappingList.size() > 0) {
					mappingListSize = mappingList.size();

					while (true) {
						completedMappingMap = FileUtils.getListOfCompletedMapping(completedMappingPath);

						if (completedMappingMap != null && completedMappingMap.size() > 0) {

							for (Integer newMappingId : mappingList) {
								try {
									String extKey = "EXT_"+newMappingId;
									String status = completedMappingMap.get(extKey);

									String filePath = FileUtils.getFilePath(completedMappingPath, extKey, status);
									FileUtils.deleteQuietly(filePath);

									if ("SUCCESS".equals(status)) {
										iterationCount++;
										future = queueSemaphore.submit(new TransportMappingThread(newMappingId, command));

										list.add(future);
									} else if ("FAIL".equals(status)) {
										loadKey = "LOAD_"+newMappingId;
										filePath = FileUtils.getFilePath(completedMappingPath, loadKey, status);
										FileUtils.touch(filePath);
										iterationCount++;
									}
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

										transKey = "TRANS_"+mappingId;
										loadKey = "LOAD_"+mappingId;

										String transFilePath = FileUtils.getFilePath(completedMappingPath, transKey, status);
										FileUtils.touch(transFilePath);

										if ("FAIL".equals(status)) {
											String loadFilePath = FileUtils.getFilePath(completedMappingPath, loadKey, status);
											FileUtils.touch(loadFilePath);
										}
										list.remove(futureMap);
										System.out.println("Transport :: "+transKey+" ==> "+status);
									}
								} catch (InterruptedException | ExecutionException e) {
									e.printStackTrace();
								}
							}
						}

						if (mappingListSize == iterationCount) {
							break;
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

		System.out.println("TransportThread Completed!");
	}

}
