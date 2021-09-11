package ru.starksoft.differ.adapter

import androidx.core.util.Pools
import androidx.core.util.Pools.SynchronizedPool
import java.util.*

/**
 * Список меток
 */
class DifferLabels {

    protected val differLabels: MutableList<Int> = ArrayList()
    fun release() {
        clear()
        labelsPool.release(this)
    }

    fun add(vararg labels: Int) {
        if (labels.isNotEmpty()) {
            for (label in labels) {
                differLabels.add(label)
            }
        }
    }

    fun add(labels: List<Int>) {
        differLabels.addAll(labels)
    }

    fun getItems() = differLabels

    fun clear() {
        differLabels.clear()
    }

    fun has(label: Int): Boolean {
        return differLabels.contains(label)
    }

    fun log(): String {
        if (differLabels.isNotEmpty()) {
            val sb = StringBuilder()
            sb.append("size=").append(differLabels.size)
            var i = 0
            val size = differLabels.size
            while (i < size) {
                sb.append(", ").append("[").append(i).append("]=").append(differLabels[i])
                i++
            }
            sb.append("}")
            return sb.toString()
        }
        return ""
    }

    companion object {

        private const val MAX_SIZE_POOL = 20
        private val labelsPool: Pools.Pool<DifferLabels> = SynchronizedPool(MAX_SIZE_POOL)
        fun obtain(): DifferLabels {
            var labels = labelsPool.acquire()
            if (labels == null) {
                labels = DifferLabels()
            }
            return labels
        }
    }
}