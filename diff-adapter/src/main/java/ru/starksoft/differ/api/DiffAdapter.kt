package ru.starksoft.differ.api

import android.util.SparseArray
import androidx.core.util.Preconditions.checkNotNull
import androidx.core.util.isEmpty
import androidx.recyclerview.widget.RecyclerView
import ru.starksoft.differ.adapter.DifferAdapter
import ru.starksoft.differ.adapter.DifferAdapterEventListener
import ru.starksoft.differ.adapter.OnClickListener
import ru.starksoft.differ.adapter.ViewHolderFactory
import ru.starksoft.differ.adapter.viewholder.DifferViewHolder
import ru.starksoft.differ.adapter.viewmodel.DifferViewModel
import ru.starksoft.differ.adapter.viewmodel.ViewModel
import ru.starksoft.differ.divider.DividerItemDecoration
import ru.starksoft.differ.utils.ExecutorHelper
import ru.starksoft.differ.utils.ExecutorHelperImpl
import java.lang.reflect.ParameterizedType

class DiffAdapter private constructor(private val dataSource: DiffAdapterDataSource) {
	private var onClickListener: OnClickListener? = null
	private var viewHolderFactory: ViewHolderFactory? = null
	private lateinit var adapterInstance: AdapterInstance

	fun withFactory(viewHolderFactory: ViewHolderFactory): DiffAdapter {
		this.viewHolderFactory = checkNotNull(viewHolderFactory)
		return this
	}

	@Suppress("UNCHECKED_CAST")
	fun withViewHolders(vararg classes: Class<out DifferViewHolder<out ViewModel>>): DiffAdapter {
		val sparseArray = SparseArray<Class<out DifferViewHolder<out ViewModel>>>()

		for (clazz in classes) {
			val type = (clazz.genericSuperclass as ParameterizedType).actualTypeArguments[0]
			val itemViewType = DifferViewModel.getItemViewType(type as Class<ViewModel>)

			if (clazz.constructors.isEmpty()) {
				throw IllegalStateException("ViewHolder does not have any constructors. Use @Keep annotation on ViewHolder class")
			}
			sparseArray.put(itemViewType, clazz)
		}

		if (sparseArray.isEmpty()) {
			throw IllegalStateException("We need at least one ViewHolder to proceed")
		}

		this.viewHolderFactory = ViewHolderFactory { parent, viewType, onClickListener ->
			return@ViewHolderFactory sparseArray[viewType]?.let {
				it.constructors[0].newInstance(parent, onClickListener) as DifferViewHolder<*>
			} ?: throw IllegalStateException("Unknown viewType=$viewType at ${javaClass.simpleName}")
		}
		return this
	}

	fun withClickListener(onClickListener: OnClickListener): DiffAdapter {
		this.onClickListener = checkNotNull(onClickListener)
		return this
	}

	fun createAdapter(logger: Logger = LoggerImpl.instance): DiffAdapter {
		adapterInstance = AdapterInstance(viewHolderFactory!!, onClickListener, logger, ExecutorHelperImpl()) {
			dataSource.onDetach()
			onClickListener = null
			viewHolderFactory = null
		}
		return this
	}

	@JvmOverloads
	fun attachTo(
		recyclerView: RecyclerView, differAdapterEventListener: DifferAdapterEventListener? = null,
		logger: Logger = LoggerImpl.instance,
		refreshAdapterOnAttach: Boolean = false
	) {

		differAdapterEventListener?.let { adapterInstance.setEventListener(it) }

		dataSource.setOnAdapterRefreshedListener(OnAdapterRefreshedListener { viewModels, labels ->
			adapterInstance.update(viewModels, labels)
		})

		if (recyclerView.adapter != adapterInstance) {
			recyclerView.adapter = adapterInstance
			recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, adapterInstance))
		}

		if (refreshAdapterOnAttach) {
			dataSource.refreshAdapter()
		}
	}

	private class AdapterInstance(
		viewHolderFactory: ViewHolderFactory,
		onClickListener: OnClickListener?,
		logger: Logger,
		executorHelper: ExecutorHelper,
		private val listener: () -> Unit
	) : DifferAdapter(
		viewHolderFactory,
		onClickListener,
		logger = logger,
		executorHelper = executorHelper
	) {

		override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
			super.onDetachedFromRecyclerView(recyclerView)
			listener()
		}
	}

	companion object {
		fun create(dataSource: DiffAdapterDataSource): DiffAdapter {
			return DiffAdapter(dataSource)
		}
	}
}
