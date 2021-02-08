package ru.starksoft.differ.utils.hash

import androidx.annotation.VisibleForTesting
import java.util.concurrent.ConcurrentHashMap

class Cache {

    private val map = ConcurrentHashMap<String, Int>()

    val cacheSize: Int
        @VisibleForTesting
        get() = map.size

    fun getValue(key: String): Int {
        var value = map[key]

        if (value == null) {
            value = key.hashCode()
            map[key] = value
        }

        return value
    }
}
