package ru.starksoft.differ.sample.screens.sample.adapter

import android.content.ContentResolver
import android.net.Uri
import ru.starksoft.differ.adapter.viewmodel.ViewModelReused
import ru.starksoft.differ.adapter.viewmodel.addEx
import ru.starksoft.differ.api.DiffAdapterDataSource
import ru.starksoft.differ.api.Logger
import ru.starksoft.differ.divider.DividerType
import ru.starksoft.differ.sample.screens.sample.adapter.viewmodel.SampleViewModel
import ru.starksoft.differ.sample.screens.sample.dialogs.ActionsBottomSheet
import ru.starksoft.differ.utils.ExecutorHelper
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class DiffAdapterDataSourceImpl(executorHelper: ExecutorHelper, logger: Logger) :
    DiffAdapterDataSource(executorHelper, logger) {

    private val data = ArrayList<SampleEntity>()
    private val ids = AtomicInteger(0)

    override fun buildViewModelList(viewModelReused: ViewModelReused) {
        synchronized(this) {
            if (data.size > 0) {
                //viewModelReused.addEx(data.size) { hash -> DataInfoViewModel(hash, DATA_INFO_ID, "Items count: ${data.size}") }
            }

            for (datum in data) {
                //				if (datum.id % CATS_COUNT == 1) {
                //					val text = "This is header"
                //					viewModelReused.addEx(text) { hash -> HeaderViewModel(hash, text) }
                //				}

                viewModelReused.addEx(datum.id, datum.name) { hash ->
                    SampleViewModel(
                        hash,
                        datum.id,
                        datum.name,
                        getRawUri(String.format(TEMPLATE, datum.id % CATS_COUNT)).toString(),
                        DividerType.PADDING_16,
                        datum.scrollTo
                    )
                }
            }
        }
    }

    fun populate(count: Int) {
        synchronized(this) {
            for (i in 0 until count) {
                val id = ids.incrementAndGet()
                data.add(SampleEntity(id, "String id=$id"))
            }
        }
    }

    fun addNewItems(count: Int) {
        synchronized(this) {
            for (i in 0 until count) {
                val id = ids.incrementAndGet()
                data.add(SampleEntity(id, "String id=$id"))
            }

            refreshAdapter()
        }
    }

    fun remove(id: Int) {
        synchronized(this) {
            val iterator = data.iterator()

            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next.id == id) {
                    iterator.remove()
                    break
                }
            }

            if (data.isEmpty()) {
                ids.set(0)
            }

            refreshAdapter()
        }
    }

    fun addItems(action: ActionsBottomSheet.Actions, count: Int) {
        synchronized(this) {
            for (i in 0 until count) {
                val id = ids.incrementAndGet()
                when (action) {
                    ActionsBottomSheet.Actions.ADD_TO_START -> data.add(0, SampleEntity(id, "String id=$id", true))
                    ActionsBottomSheet.Actions.ADD_TO_CENTER -> data.add(
                        data.size / 2,
                        SampleEntity(id, "String id=$id", true)
                    )
                    ActionsBottomSheet.Actions.ADD_TO_END -> data.add(SampleEntity(id, "String id=$id", true))
                    ActionsBottomSheet.Actions.SWAP_0_TO_1 -> {
                        if (data.size > 1) {
                            Collections.swap(data, 0, 1)
                            refreshAdapter()
                        }
                    }
                }
            }
            refreshAdapter()
        }
    }

    fun onSortChanged(from: Int, to: Int) {
        synchronized(this) {
            Collections.swap(data, from, to)
        }
    }

    private data class SampleEntity(val id: Int, val name: String, val scrollTo: Boolean = false)

    companion object {

        private const val CATS_COUNT = 3
        private const val DATA_INFO_ID = -1
        private const val TEMPLATE = "cat_%s"

        fun create(executorHelper: ExecutorHelper, logger: Logger): DiffAdapterDataSourceImpl {
            return DiffAdapterDataSourceImpl(executorHelper, logger)
        }

        fun getRawUri(filename: String): Uri {
            return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://ru.starksoft.differ.sample" + "/raw/" + filename)
        }
    }
}
