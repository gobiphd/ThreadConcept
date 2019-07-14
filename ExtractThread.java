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

	private List<Integer> mappingList;

	public ExtractThread(String strExtractThreadCount, List<Integer> mappingList, String command) {
		this.strExtractThreadCount = strExtractThreadCount;
		this.mappingList = mappingList;
		this.command = command;
	}

	@Override
	public void run() {
		System.out.println("ExtractThread Started!");
		int extractThreadCount = 0;
		String cacheKey = "";
		String transCacheKey = "";
		List<Future<Map<String, Object>>> list = new CopyOnWriteArrayList<>();
		Future<Map<String, Object>> future = null;
		ExecutorService executor = null;
		QueueSemaphore queueSemaphore = null;
		CacheManager cacheManager = null;
		try {
			executor = Executors.newCachedThreadPool();
			cacheManager = CacheManager.getInstance();

			if (strExtractThreadCount != null && !strExtractThreadCount.isEmpty()) {
				extractThreadCount = Integer.parseInt(strExtractThreadCount);
			}

			if (executor != null) {
				queueSemaphore = new QueueSemaphore(executor, extractThreadCount);
			}

			if (queueSemaphore != null) {
				for (Integer newMappingId : mappingList) {
					try {
						future = queueSemaphore.submit(new ExtractMappingThread(newMappingId, command));

						list.add(future);
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

								cacheKey = "EXT_"+mappingId;
								transCacheKey = "TRANS_"+mappingId;

								cacheManager.put(cacheKey, status);

								if ("FAIL".equals(status)) {
									cacheManager.put(transCacheKey, status);
								}
								list.remove(futureMap);
								System.out.println("Extract :: "+cacheKey+" ==> "+status);
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
