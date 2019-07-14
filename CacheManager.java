package com.gobi.thread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CacheManager {

	private static CacheManager instance;
	private static Object monitor = new Object();
	private Map<String, Object> cache = new ConcurrentHashMap<>();

	private CacheManager() {}

	public void put(String cacheKey, Object value) {
		cache.put(cacheKey, value);
	}

	public Object get(String cacheKey) {
		return cache.get(cacheKey);
	}

	public void remove(String cacheKey) {
		cache.remove(cacheKey);
	}

	public void clear() {
		cache.clear();
	}

	public int size() {
		return cache.size();
	}

	public static CacheManager getInstance() {
		if (instance == null) {
			synchronized (monitor) {
				if (instance == null) {
					instance = new CacheManager();
				}
			}
		}
		return instance;
	}

}
