package ru.starksoft.differ.utils.hash;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

public final class Cache {

	private final Map<String, Integer> map = new ConcurrentHashMap<>();

	public final int getValue(@NonNull String key) {
		Integer value = map.get(key);

		if (value == null) {
			value = key.hashCode();
			map.put(key, value);
		}

		return value;
	}


	@VisibleForTesting
	public int getCacheSize() {
		return map.size();
	}
}
