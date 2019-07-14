package com.gobi.thread;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class LoadingThread extends Thread {

	private String strLoadingThreadCount;

	private String command;

	private List<Integer> mappingList;

	public LoadingThread(String strLoadingThreadCount, List<Integer> mappingList, String command) {
		this.strLoadingThreadCount = strLoadingThreadCount;
		this.mappingList = mappingList;
		this.command = command;
	}

	@Override
	public void run() {
		System.out.println("LoadingThread Started!");
		int loadingThreadCount = 0;
		int iterationCount = 0;
		int mappingListSize = 0;
		String cacheKey = "";
		List<Future<Map<String, Object>>> list = new CopyOnWriteArrayList<>();
		Future<Map<String, Object>> future = null;
		ExecutorService executor = null;
		QueueSemaphore queueSemaphore = null;
		CacheManager cacheManager = null;
		try {
			executor = Executors.newCachedThreadPool();
			cacheManager = CacheManager.getInstance();

			if (strLoadingThreadCount != null && !strLoadingThreadCount.isEmpty()) {
				loadingThreadCount = Integer.parseInt(strLoadingThreadCount);
			}

			if (executor != null) {
				queueSemaphore = new QueueSemaphore(executor, loadingThreadCount);
			}

			if (queueSemaphore != null) {

				if (mappingList != null && mappingList.size() > 0) {
					mappingListSize = mappingList.size();

					while (true) {

						for (Integer newMappingId : mappingList) {
							try {
								String extCacheKey = "TRANS_"+newMappingId;
								String status = (String)cacheManager.get(extCacheKey);

								if ("SUCCESS".equals(status)) {
									cacheManager.remove(extCacheKey);
									iterationCount++;
									future = queueSemaphore.submit(new LoadingMappingThread(newMappingId, command));

									list.add(future);
								} else if ("FAIL".equals(status)) {
									cacheManager.remove(extCacheKey);
									iterationCount++;
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

						if (list != null && list.size() > 0) {
							for (Future<Map<String, Object>> futureMap : list) {
								try {
									Map<String, Object> returnMap = futureMap.get();

									if (returnMap != null && returnMap.size() > 0) {
										String status = (String)returnMap.get("STATUS");
										int mappingId = (int)returnMap.get("MAPPING_ID");

										cacheKey = "LOAD_"+mappingId;
										cacheManager.put(cacheKey, status);
										list.remove(futureMap);
										System.out.println("Loading :: "+cacheKey+" ==> "+status);
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

		System.out.println("LoadingThread Completed!");
	}

}