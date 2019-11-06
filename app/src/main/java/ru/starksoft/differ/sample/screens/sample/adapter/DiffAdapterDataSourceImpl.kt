package ru.starksoft.differ.sample.screens.sample.adapter

import android.content.ContentResolver
import android.net.Uri
import ru.starksoft.differ.presenter.DiffAdapterDataSource
import ru.starksoft.differ.sample.screens.sample.adapter.viewmodel.SampleViewModel
import ru.starksoft.differ.utils.ExecutorHelper
import ru.starksoft.differ.viewmodel.ViewModelReused
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class DiffAdapterDataSourceImpl(executorHelper: ExecutorHelper) : DiffAdapterDataSource(executorHelper) {
	private val data = ArrayList<SampleEntity>()
	private val ids = AtomicInteger(0)

	override fun buildViewModelList(viewModelReused: ViewModelReused) {

		synchronized(data) {
			for (datum in data) {
				val parse = getRawUri(String.format(TEMPLATE, datum.id % 3))
				viewModelReused.add(SampleViewModel(0, datum.id, datum.name, parse.toString()))
			}
		}
	}

	fun populate(count: Int) {
		for (i in 0 until count) {
			val id = ids.incrementAndGet()
			synchronized(data) {
				data.add(SampleEntity(id, "String id=$id"))
			}
		}
	}

	fun addNewItems(count: Int) {
		for (i in 0 until count) {
			val id = ids.incrementAndGet()
			synchronized(data) {
				data.add(i, SampleEntity(id, "String id=$id"))
			}
		}

		refreshAdapter()
	}

	fun remove(id: Int) {
		val iterator = data.iterator()

		synchronized(data) {
			while (iterator.hasNext()) {
				val next = iterator.next()
				if (next.id == id) {
					iterator.remove()
					break
				}
			}
		}
		refreshAdapter()
	}

	private data class SampleEntity(val id: Int, val name: String)

	companion object {

		private const val TEMPLATE = "cat_%s"

		fun create(executorHelper: ExecutorHelper): DiffAdapterDataSourceImpl {
			return DiffAdapterDataSourceImpl(executorHelper)
		}

		fun getRawUri(filename: String): Uri {
			return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://ru.starksoft.differ.sample" + "/raw/" + filename)
		}
	}
}
