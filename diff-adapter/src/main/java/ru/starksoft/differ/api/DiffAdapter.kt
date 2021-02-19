package ru.starksoft.differ.api

import android.annotation.SuppressLint
import android.util.SparseArray
import androidx.core.util.Preconditions.checkNotNull
import androidx.core.util.isEmpty
import androidx.recyclerview.widget.ItemTouchHelper
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

@SuppressLint("RestrictedApi")
class DiffAdapter private constructor(private val dataSource: DiffAdapterDataSource) {

    private var onClickListener: OnClickListener? = null
    private var viewHolderFactory: ViewHolderFactory? = null
    private var itemTouchHelper: ItemTouchHelper? = null
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

    fun withItemTouchHelper(itemTouchHelper: ItemTouchHelper): DiffAdapter {
        this.itemTouchHelper = checkNotNull(itemTouchHelper)
        return this
    }

    fun initAdapter(presetDataFromDataSource: Boolean = false, logger: Logger = LoggerImpl.INSTANCE): DiffAdapter {
        val cachedData = if (presetDataFromDataSource) dataSource.getPreviousViewModels() else ArrayList()
        adapterInstance =
            AdapterInstance(
                cachedData,
                viewHolderFactory!!,
                onClickListener,
                logger,
                ExecutorHelperImpl(),
                object : RecyclerViewEvents {
                    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                        dataSource.onAttach()
                    }

                    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
                        dataSource.onDetach()
                        onClickListener = null
                        viewHolderFactory = null
                    }
                })
        return this
    }

    @Deprecated(message = "Replaced with initAdapter()", replaceWith = ReplaceWith("initAdapter()"))
    fun createAdapter(presetDataFromDataSource: Boolean = false, logger: Logger = LoggerImpl.INSTANCE): DiffAdapter {
        return initAdapter(presetDataFromDataSource, logger)
    }

    @JvmOverloads
    fun attachTo(
        recyclerView: RecyclerView, differAdapterEventListener: DifferAdapterEventListener? = null,
        refreshAdapterOnAttach: Boolean = false
    ) {
        if (!::adapterInstance.isInitialized) {
            throw IllegalStateException("adapterInstance is not initialized, did you call initAdapter()?")
        }

        differAdapterEventListener?.let { adapterInstance.setEventListener(it) }

        dataSource.setOnAdapterRefreshedListener { viewModels, labels, dontTriggerMoves ->
            adapterInstance.update(viewModels, labels, dontTriggerMoves)
        }

        if (recyclerView.adapter != adapterInstance) {
            recyclerView.adapter = adapterInstance
            recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, adapterInstance))
        }

        itemTouchHelper?.attachToRecyclerView(recyclerView)

        if (refreshAdapterOnAttach) {
            dataSource.refreshAdapter()
        }
    }

    private class AdapterInstance(
        cachedData: List<ViewModel>,
        viewHolderFactory: ViewHolderFactory,
        onClickListener: OnClickListener?,
        logger: Logger,
        executorHelper: ExecutorHelper,
        private val recyclerViewEvents: RecyclerViewEvents
    ) : DifferAdapter(
        viewHolderFactory,
        onClickListener,
        list = cachedData,
        logger = logger,
        executorHelper = executorHelper
    ) {

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            recyclerViewEvents.onDetachedFromRecyclerView(recyclerView)
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            recyclerViewEvents.onAttachedToRecyclerView(recyclerView)
        }
    }

    interface RecyclerViewEvents {

        fun onDetachedFromRecyclerView(recyclerView: RecyclerView)
        fun onAttachedToRecyclerView(recyclerView: RecyclerView)
    }

    companion object {

        @JvmStatic
        fun create(dataSource: DiffAdapterDataSource): DiffAdapter {
            return DiffAdapter(dataSource)
        }
    }
}
