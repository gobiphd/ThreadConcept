package com.gobi.thread;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BaseWrapper {

	public static void main(String[] args) {
		System.out.println("BaseWrapper Started!  ==> "+ new Date());
		String strExtractThreadCount = "1";
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

			extThread = new ExtractThread(strExtractThreadCount, mappingList, command);
			transportThread = new TransportThread(strExtractThreadCount, mappingList, command);
			loadingThread = new LoadingThread(strExtractThreadCount, mappingList, command);

			extThread.start();
			transportThread.start();
			loadingThread.start();

			extThread.join();
			transportThread.join();
			loadingThread.join();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("BaseWrapper Completed! => "+ new Date());
	}

}
