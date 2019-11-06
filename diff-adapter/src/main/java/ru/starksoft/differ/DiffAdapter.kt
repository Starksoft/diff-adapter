package ru.starksoft.differ

import androidx.core.util.Preconditions.checkNotNull
import androidx.recyclerview.widget.RecyclerView
import ru.starksoft.differ.adapter.DifferAdapter
import ru.starksoft.differ.adapter.DifferAdapterEventListener
import ru.starksoft.differ.adapter.OnClickListener
import ru.starksoft.differ.adapter.ViewHolderFactory
import ru.starksoft.differ.divider.DividerItemDecoration
import ru.starksoft.differ.presenter.DiffAdapterDataSource
import ru.starksoft.differ.presenter.OnAdapterRefreshedListener
import ru.starksoft.differ.utils.ExecutorHelper
import ru.starksoft.differ.utils.ExecutorHelperImpl

class DiffAdapter private constructor(private val dataSource: DiffAdapterDataSource) {
	private var onClickListener: OnClickListener? = null
	private var viewHolderFactory: ViewHolderFactory? = null

	fun withFactory(viewHolderFactory: ViewHolderFactory): DiffAdapter {
		this.viewHolderFactory = checkNotNull(viewHolderFactory)
		return this
	}

	fun withClickListener(onClickListener: OnClickListener): DiffAdapter {
		this.onClickListener = checkNotNull(onClickListener)
		return this
	}

	@JvmOverloads
	fun attachTo(
		recyclerView: RecyclerView, differAdapterEventListener: DifferAdapterEventListener? = null,
		logger: Logger? = null
	) {

		val adapter = AdapterInstance(checkNotNull(viewHolderFactory), checkNotNull(onClickListener), logger, ExecutorHelperImpl())

		if (differAdapterEventListener != null) {
			adapter.setEventListener(differAdapterEventListener)
		}

		dataSource.setOnAdapterRefreshedListener(OnAdapterRefreshedListener { viewModels, labels -> adapter.update(viewModels, labels) })

		recyclerView.adapter = adapter
		recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, adapter))
		dataSource.refreshAdapter()
	}

	private class AdapterInstance(
		viewHolderFactory: ViewHolderFactory, onClickListener: OnClickListener,
		logger: Logger?, executorHelper: ExecutorHelper
	) : DifferAdapter(viewHolderFactory, onClickListener, logger, executorHelper)

	companion object {

		fun create(dataSource: DiffAdapterDataSource): DiffAdapter {
			return DiffAdapter(dataSource)
		}
	}
}
