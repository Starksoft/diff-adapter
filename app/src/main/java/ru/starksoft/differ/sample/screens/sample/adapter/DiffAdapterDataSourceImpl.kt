package ru.starksoft.differ.sample.screens.sample.adapter

import android.annotation.SuppressLint
import android.content.ContentResolver.SCHEME_ANDROID_RESOURCE
import android.content.Context
import android.net.Uri
import android.preference.PreferenceManager
import ru.starksoft.differ.adapter.viewmodel.ViewModelReused
import ru.starksoft.differ.adapter.viewmodel.addEx
import ru.starksoft.differ.api.DiffAdapterDataSource
import ru.starksoft.differ.api.Logger
import ru.starksoft.differ.divider.DividerType
import ru.starksoft.differ.sample.screens.sample.adapter.viewmodel.DataInfoViewModel
import ru.starksoft.differ.sample.screens.sample.adapter.viewmodel.GroupViewModel
import ru.starksoft.differ.sample.screens.sample.adapter.viewmodel.HeaderViewModel
import ru.starksoft.differ.sample.screens.sample.adapter.viewmodel.SampleViewModel
import ru.starksoft.differ.sample.screens.sample.dialogs.ActionsBottomSheet
import ru.starksoft.differ.utils.ExecutorHelper
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class DiffAdapterDataSourceImpl(context: Context, executorHelper: ExecutorHelper, logger: Logger) :
    DiffAdapterDataSource(executorHelper, logger) {

    private val data = ArrayList<SampleEntity>()
    private val ids = AtomicInteger(0)
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun buildViewModelList(viewModelReused: ViewModelReused) {
        synchronized(data) {
            if (data.size > 0) {
                viewModelReused.addEx(data.size) { hash ->
                    DataInfoViewModel(
                        hash,
                        DATA_INFO_ID,
                        "Items count: ${data.size}"
                    )
                }
            }

            fun addGroup(key: String, callback: () -> Unit) {
                val groupExpanded = sharedPreferences.getBoolean(key, true)
                viewModelReused.add(GroupViewModel(0, key, groupExpanded))
                if (groupExpanded) {
                    callback()
                }
            }

            addGroup("Some group") {
                viewModelReused.add(
                    SampleViewModel(
                        0,
                        -1,
                        "Cat in group",
                        getRawUri(String.format(TEMPLATE, 2)).toString(),
                        DividerType.LEFT_PADDING_48,
                        false
                    )
                )
            }

            for (datum in data) {
                if (datum.id % CATS_COUNT == 1) {
                    val text = "This is header"
                    viewModelReused.addEx(text) { hash -> HeaderViewModel(hash, text) }
                }

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
        synchronized(data) {
            for (i in 0 until count) {
                val id = ids.incrementAndGet()
                data.add(SampleEntity(id, "String id=$id"))
            }
        }
    }

    fun addNewItems(count: Int) {
        synchronized(data) {
            for (i in 0 until count) {
                val id = ids.incrementAndGet()
                data.add(SampleEntity(id, "String id=$id"))
            }

            refreshAdapter()
        }
    }

    fun remove(id: Int) {
        synchronized(data) {
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
        }
        refreshAdapter()
    }

    fun addItems(action: ActionsBottomSheet.Actions, count: Int) {
        synchronized(data) {
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
        synchronized(data) {
            Collections.swap(data, from, to)
        }
    }

    @SuppressLint("ApplySharedPref")
    fun groupAction(key: String, expanded: Boolean) {
        sharedPreferences.edit().putBoolean(key, !expanded).commit()
        refreshAdapter()
    }

    private data class SampleEntity(val id: Int, val name: String, val scrollTo: Boolean = false)

    companion object {

        private const val CATS_COUNT = 3
        private const val DATA_INFO_ID = -1
        const val TEMPLATE = "cat_%s"

        fun getRawUri(filename: String): Uri {
            return Uri.parse("$SCHEME_ANDROID_RESOURCE://ru.starksoft.differ.sample/raw/$filename")
        }
    }
}
