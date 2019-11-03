package ru.starksoft.differ.utils.hash;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ru.starksoft.differ.viewmodel.ViewModel;

public final class HashCode {

	public static final int NONE_HASHCODE = 0;

	private final static Cache cache = new Cache();

	private HashCode() {
		throw new UnsupportedOperationException();
	}

	public static int get(@Nullable Object... list) {
		return get(null, list);
	}

	public static int get(@Nullable Class clazz, @Nullable Object... list) {
		if (list == null && clazz == null) {
			return NONE_HASHCODE;
		}

		int result = clazz == null ? 1 : 31 + clazz.getName().hashCode();
		if (list != null) {
			result = 31 * result + Arrays.deepHashCode(list);
		}
		return result;
	}

	public static <M extends ViewModel> int get(@NonNull Class<M> clazz) {
		return cache.getValue(clazz.getName());
	}


}
