package ru.starksoft.differ.adapter

import androidx.core.util.Pools
import androidx.core.util.Pools.SynchronizedPool
import ru.starksoft.differ.adapter.viewmodel.ViewModel
import java.util.*

/**
 * Связка списка ViewModel'ей и меток обновления
 *
 *
 * Метки передаеются в refreshAdapter(метка) и обрабатываюся в completedUpdateAdapter()
 */
internal class DifferRefreshingData private constructor() {

    private val items: MutableList<ViewModel> = ArrayList()
    var labels: DifferLabels
        private set

    init {
        labels = DifferLabels()
    }

    fun release() {
        items.clear()
        labels.release()
        refreshingDataPool.release(this)
    }

    fun init(items: List<ViewModel>, labels: DifferLabels) {
        this.items.clear()
        this.items.addAll(items)
        this.labels = DifferLabels()
        this.labels.add(labels.getItems())
    }

    fun getItems(): List<ViewModel> {
        return items
    }

    companion object {

        private const val MAX_SIZE_POOL = 20
        private val refreshingDataPool: Pools.Pool<DifferRefreshingData> =
            SynchronizedPool(MAX_SIZE_POOL)

        fun obtain(): DifferRefreshingData {
            var differRefreshingData = refreshingDataPool.acquire()
            if (differRefreshingData == null) {
                differRefreshingData = DifferRefreshingData()
            }
            return differRefreshingData
        }
    }
}
