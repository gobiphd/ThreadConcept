package com.gobi.thread;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BaseWrapper {

	public static void main(String[] args) {
		System.out.println("BaseWrapper Started!  ==> "+ new Date());
		String strExtractThreadCount = "1";
		String strExtractionRequired = "Y";
		String strTransportationRequired = "Y";
		String strDataloadingRequired = "Y";
		String userId = "100";
		String datasetId = "101";
		List<Integer> mappingList = new ArrayList<>();
		String command = "java -jar /home/gobinath/Desktop/TempJar.jar";
		ExtractThread extThread = null;
		TransportThread transportThread = null;
		LoadingThread loadingThread = null;
		try {
			mappingList.add(1);
			/*
			 * mappingList.add(2); mappingList.add(3);
			 */

			if ("Y".equals(strExtractionRequired) && "N".equals(strTransportationRequired) && "Y".equals(strDataloadingRequired)) {
				System.out.println("Wrong data!!!");
			} else {
				String tempFolderPath = userId + "/" + datasetId;
				FileUtils.createDirectoriesInTempPath(tempFolderPath);

				if ("Y".equals(strExtractionRequired)) {
					extThread = new ExtractThread(strExtractThreadCount, mappingList, command, userId, datasetId);
					extThread.setName("ExtractThread");
					extThread.start();
				}

				if ("Y".equals(strTransportationRequired)) {
					transportThread = new TransportThread(strExtractThreadCount, mappingList, command, userId, datasetId);
					transportThread.setName("TransportationThread");
					transportThread.start();
				}

				if ("Y".equals(strDataloadingRequired)) {
					loadingThread = new LoadingThread(strExtractThreadCount, mappingList, command, userId, datasetId);
					loadingThread.setName("LoadingThread");
					loadingThread.start();
				}

				if ("Y".equals(strExtractionRequired)) {
					extThread.join();
				}

				if ("Y".equals(strTransportationRequired)) {
					transportThread.join();
				}

				if ("Y".equals(strDataloadingRequired)) {
					loadingThread.join();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("BaseWrapper Completed! => "+ new Date());
	}

}
