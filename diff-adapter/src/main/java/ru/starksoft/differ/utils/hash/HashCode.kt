package ru.starksoft.differ.utils.hash

import ru.starksoft.differ.adapter.viewmodel.ViewModel

object HashCode {

	const val NONE_HASHCODE = 0
	private val cache = Cache()
	@JvmStatic
	operator fun get(vararg list: Any?): Int {
		return get(null, *list)
	}

	@JvmStatic
	operator fun get(clazz: Class<*>?, vararg list: Any?): Int {
		if (list == null && clazz == null) {
			return NONE_HASHCODE
		}

		var result = if (clazz == null) 1 else 31 + clazz.name.hashCode()
		if (list != null) {
			result = 31 * result + list.contentDeepHashCode()
		}
		return result
	}

	@JvmStatic
	operator fun <M : ViewModel> get(clazz: Class<M>): Int {
		return cache.getValue(clazz.name)
	}

}
