package ru.starksoft.differ.adapter

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.annotation.AnyThread
import androidx.annotation.IntRange
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.core.util.Pair
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import ru.starksoft.commons.system.ThreadUtils.checkMainThread
import ru.starksoft.differ.adapter.viewholder.DifferViewHolder
import ru.starksoft.differ.adapter.viewmodel.DifferViewModel.Companion.getItemViewType
import ru.starksoft.differ.adapter.viewmodel.ViewModel
import ru.starksoft.differ.adapter.viewmodel.ViewModelReused
import ru.starksoft.differ.api.Logger
import ru.starksoft.differ.divider.DividerType
import ru.starksoft.differ.utils.ExecutorHelper
import ru.starksoft.differ.utils.diff.DiffCallback
import java.util.*

abstract class DifferAdapter(
    private val viewHolderFactory: ViewHolderFactory,
    private val onClickListener: OnClickListener? = null,
    list: List<ViewModel>? = null,
    private val logger: Logger? = null,
    private val executorHelper: ExecutorHelper
) : RecyclerView.Adapter<DifferViewHolder<in ViewModel>>(), OnClickListener {

    private val data: MutableList<ViewModel> = ArrayList()
    private val pendingUpdates: Deque<DifferRefreshingData> = ArrayDeque()
    private val handler = Handler(Looper.getMainLooper())
    private val listOnCompletedUpdateAdapterListener: MutableList<OnCompletedUpdateAdapterListener> = ArrayList()
    private var eventListener: DifferAdapterEventListener? = null
    private var recyclerView: RecyclerView? = null
    private var dontTriggerMoves: Boolean = false
    private val lock = Object()

    init {
        list?.let { data.addAll(it) }
    }

    fun setEventListener(eventListener: DifferAdapterEventListener) {
        this.eventListener = eventListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DifferViewHolder<in ViewModel> {
        return viewHolderFactory.create(parent, viewType, this)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        executorHelper.destroy()
        pendingUpdates.clear()
        handler.removeCallbacksAndMessages(null)
        this.recyclerView = null
        super.onDetachedFromRecyclerView(recyclerView)
    }

    @SafeVarargs
    fun setMaxRecycledViews(
        recyclerView: RecyclerView,
        vararg maxRecycledViews: Pair<Class<out ViewModel>?, Int?>
    ) {
        val viewPool = recyclerView.recycledViewPool
        for (pair in maxRecycledViews) {
            val first = pair.first
            val second = pair.second
            check(!(first == null || second == null)) { "Params of pair is null" }
            viewPool.setMaxRecycledViews(getItemViewType(first), second)
        }
    }

    fun addOnCompletedUpdateAdapterListener(onCompletedUpdateAdapterListener: OnCompletedUpdateAdapterListener) {
        synchronized(lock) { listOnCompletedUpdateAdapterListener.add(onCompletedUpdateAdapterListener) }
    }

    fun removeAllOnCompletedUpdateAdapterListener() {
        synchronized(lock) { listOnCompletedUpdateAdapterListener.clear() }
    }

    private fun getItem(
        @IntRange(from = 0)
        position: Int
    ): ViewModel {
        synchronized(lock) {
            val viewModel = data.getOrNull(position)
            if (viewModel == null) {
                val deepToString = data.toTypedArray().contentDeepToString()
                val message = "getItem: data.size=" +
                        data.size +
                        ", position=" +
                        position +
                        ", deepToString=" +
                        deepToString
                throw IllegalStateException("viewModel is null, this isn't normal.\n$message")
            }

            return viewModel
        }
    }

    private val lastItem: ViewModel
        get() {
            synchronized(lock) { return data.last() }
        }

    fun getPosition(item: ViewModel?): Int {
        synchronized(lock) { return data.indexOf(item) }
    }

    override fun onBindViewHolder(holder: DifferViewHolder<in ViewModel>, position: Int) {
        holder.bind(getItem(position), null)
    }

    override fun onBindViewHolder(holder: DifferViewHolder<in ViewModel>, position: Int, payloads: List<Any>) {
        holder.bind(getItem(position), getPayloads(payloads))
    }

    private fun getPayloads(payloads: List<Any>): Bundle? {
        if (payloads.isEmpty()) {
            return null
        }
        val item = payloads[0]
        return if (item !is Bundle) null else item
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).getItemViewType()
    }

    @IntRange(from = 0)
    override fun getItemCount(): Int {
        synchronized(lock) {
            return data.size
        }
    }

    fun isEmpty() = itemCount < 1

    private fun notifyOnAdapterUpdatedListeners(viewModelLabels: DifferLabels) {
        synchronized(lock) {
            for (listener in listOnCompletedUpdateAdapterListener) {
                listener.completedUpdateAdapter(viewModelLabels)
            }
        }
    }

    @UiThread
    fun update(viewModels: List<ViewModel>, labels: DifferLabels, dontTriggerMoves: Boolean) {
        synchronized(lock) {
            this.dontTriggerMoves = dontTriggerMoves
            val differRefreshingData = DifferRefreshingData.obtain()
            differRefreshingData.init(viewModels, labels)
            labels.release()
            logger?.d(
                TAG,
                String.format(
                    LOG_UPDATE_TEMPLATE,
                    javaClass.simpleName,
                    "update",
                    differRefreshingData.labels.log()
                )
            )
            pendingUpdates.add(differRefreshingData)
            if (pendingUpdates.size == 1) {
                internalUpdate(differRefreshingData)
            }
        }
    }

    @AnyThread
    private fun internalUpdate(differRefreshingData: DifferRefreshingData) {
        executorHelper.submit {
            synchronized(lock) {
                logger?.d(
                    TAG, String.format(
                        LOG_UPDATE_TEMPLATE,
                        javaClass.simpleName,
                        "internalUpdate",
                        differRefreshingData.labels.log()
                    )
                )
                val diffResult = DiffUtil.calculateDiff(
                    DiffCallback(ArrayList(data), differRefreshingData.getItems()),
                    !dontTriggerMoves
                )
                handler.post {
                    dispatchUpdates(differRefreshingData, diffResult)
                    processQueue()
                }
            }
        }
    }

    @UiThread
    private fun processQueue() {
        checkMainThread()
        if (!pendingUpdates.isEmpty()) {
            pendingUpdates.remove()
            if (!pendingUpdates.isEmpty()) {
                if (pendingUpdates.size > 1) {
                    val last = pendingUpdates.pollLast()
                    while (!pendingUpdates.isEmpty()) {
                        pendingUpdates.pop().release()
                    }
                    pendingUpdates.add(last)
                }
                pendingUpdates.peek()?.let { internalUpdate(it) }
            }
        }
    }

    @UiThread
    private fun dispatchUpdates(differRefreshingData: DifferRefreshingData, diffResult: DiffUtil.DiffResult) {
        synchronized(lock) {
            checkMainThread()
            eventListener?.onBeforeStarted()
            data.clear()
            data.addAll(differRefreshingData.getItems())
            diffResult.dispatchUpdatesTo(createListUpdateCallback())
            notifyOnAdapterUpdatedListeners(differRefreshingData.labels)
            eventListener?.onFinished(differRefreshingData.getItems())
            logger?.d(TAG, "Adapter::" + javaClass.simpleName + " DRAW " + differRefreshingData.labels.log())
            differRefreshingData.release()
        }
    }

    private fun createListUpdateCallback(): ListUpdateCallback {
        return object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
                logger?.d(
                    TAG,
                    String.format(
                        LOG_CALLBACK_TEMPLATE,
                        "onInserted",
                        position,
                        count,
                        this@DifferAdapter
                    )
                )
                notifyItemRangeInserted(position, count)
                synchronized(lock) { findItemAndPerformScroll(position, count) }
                eventListener?.onInserted(position, count)
            }

            override fun onRemoved(position: Int, count: Int) {
                logger?.d(
                    TAG,
                    String.format(
                        LOG_CALLBACK_TEMPLATE,
                        "onRemoved",
                        position,
                        count,
                        this@DifferAdapter
                    )
                )
                notifyItemRangeRemoved(position, count)
                eventListener?.onRemoved(position, count)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                logger?.d(TAG, "[ADAPTER] onMoved fromPosition = $fromPosition toPosition = $toPosition | $this")
                notifyItemMoved(fromPosition, toPosition)
                synchronized(lock) { findItemAndPerformScroll(toPosition, 1) }
                eventListener?.onMoved(fromPosition, toPosition)
            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {
                logger?.d(TAG, "[ADAPTER] onChanged position = $position count = $count payload = $payload | $this")
                notifyItemRangeChanged(position, count, payload)
                synchronized(lock) { findItemAndPerformScroll(position, count) }
                eventListener?.onChanged(position, count, payload)
            }
        }
    }

    private fun findItemAndPerformScroll(position: Int, count: Int) {
        logger?.d(TAG, "findItemAndPerformScroll() called with: position = [$position], count = [$count]")

        for (index in position..Math.min(position + (count - 1), data.size)) {
            val viewModel = getItem(index)
            if (viewModel.needScrollTo()) {
                recyclerView?.let {
                    if (it.layoutManager is LinearLayoutManager?) {
                        val scrollStrategy = viewModel.scrollStrategy()
                        val offset = if (scrollStrategy == ViewModel.ScrollStrategy.TOP) {
                            0
                        } else {
                            it.height / 2
                        }

                        (it.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, offset)
                    } else {
                        it.scrollToPosition(position)
                    }
                }
                break
            }
        }
    }

    override fun onClick(
        @IntRange(from = 0)
        position: Int, viewModel: ViewModel, action: Int, extra: Bundle
    ): Boolean {
        return onClickListener?.onClick(position, viewModel, action, extra) == true
    }

    override fun onViewRecycled(holder: DifferViewHolder<in ViewModel>) {
        super.onViewRecycled(holder)
        holder.onUnbind()
    }

    fun getDividerType(
        @IntRange(from = 0)
        position: Int
    ): DividerType {
        val classViewModel: Class<out ViewModel>? = classViewModelForLastSpecialDivider
        if (classViewModel != null) {
            val isLastItem = position == itemCount - 2
            if (isLastItem) {
                val isLastSpecialDivider = lastItem.getItemViewType() == getItemViewType(classViewModel)
                if (isLastSpecialDivider) {
                    return DividerType.PADDING_16
                }
            }
        }
        return getItem(position).getDividerType()
    }

    /**
     * Если перед последним элементом списка нужен особый тип Divider,
     * то нужно переопределить этот метод и вернуть класс ViewModel,
     * который сигнализирует о классе ViewModel последнего элемента,
     * перед которым нужен это divider.
     *
     *
     * Например, последним элементом списка вакансий идет лоадер дозагрузки страницы с типом LoadingViewModel.
     * Перед эти лоадером divider должен иметь отступы с двух сторон
     *
     * @return Class ViewModel
     */
    protected val classViewModelForLastSpecialDivider: Class<out ViewModel>?
        get() = null

    @FunctionalInterface
    interface OnBuildAdapterListener {

        /**
         * Собирает список ViewModel'ей
         */
        @WorkerThread
        fun buildViewModelList(viewModelReused: ViewModelReused)
    }

    @FunctionalInterface
    interface OnRefreshAdapterListener {

        /**
         * Пересобирает адаптер вызывая buildViewModelList
         */
        @AnyThread
        fun refreshAdapter(vararg labels: Int, dontTriggerMoves: Boolean = false)
    }

    @FunctionalInterface
    fun interface OnCompletedUpdateAdapterListener {

        /**
         * Вызывается после окончания обновления адаптера
         */
        @UiThread
        fun completedUpdateAdapter(viewModelLabels: DifferLabels)
    }

    companion object {

        private const val TAG = "DifferAdapter"
        private const val LOG_CALLBACK_TEMPLATE = "[ADAPTER] %1\$s position = %2\$s count = %3\$s | %4\$s"
        private const val LOG_UPDATE_TEMPLATE = "Adapter::%1\$s %2\$s %3\$s"

        fun getElementInfo(
            list: List<ViewModel>,
            position: Int
        ): String {
            synchronized(DifferAdapter::class.java) {
                val stringBuilder = StringBuilder()
                val empty = list.isEmpty()
                stringBuilder.append("\n listSize = ").append("(").append(if (empty) "null" else list.size).append(")")
                if (!empty) {
                    val item = if (list.size > position) list[position] else null
                    val itemContent = item?.toString() ?: "null"
                    stringBuilder.append(" item").append("(").append(position).append(")").append(" = ")
                        .append(itemContent)
                }
                return stringBuilder.toString()
            }
        }
    }
}
