package ru.starksoft.differ.utils;

import java.util.List;

import androidx.annotation.Nullable;

public final class CollectionUtils {

	private CollectionUtils() {
		throw new UnsupportedOperationException();
	}

	@Nullable
	public static <T> T getItem(List<T> list, int index) {
		return list != null && index >= 0 && index < list.size() ? list.get(index) : null;
	}
}
