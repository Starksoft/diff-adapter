package ru.starksoft.differ.viewholder;

import androidx.annotation.NonNull;
import ru.starksoft.differ.utils.hash.Cache;

public final class ClickAction {

	private static final Cache cache = new Cache();

	private ClickAction() {
		throw new UnsupportedOperationException();
	}

	public static int getActionId(@NonNull Class clazz, @NonNull String name) {
		return cache.getValue(clazz.getName() + ":" + name);
	}
}
