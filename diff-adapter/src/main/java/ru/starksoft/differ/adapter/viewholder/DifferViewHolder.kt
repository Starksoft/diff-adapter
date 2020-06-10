package ru.starksoft.differ.adapter.viewholder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import ru.starksoft.differ.adapter.OnClickListener
import ru.starksoft.differ.adapter.viewmodel.ViewModel
import java.lang.ref.WeakReference

abstract class DifferViewHolder<M : ViewModel>(@LayoutRes layout: Int, parent: ViewGroup, onClickListener: OnClickListener?) :
	RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false)) {

	private val adapterWeakReference = if (onClickListener != null) WeakReference(onClickListener) else null

	lateinit var viewModel: M
		private set

	init {
		this.setBinder(itemView)
	}

	protected open fun setBinder(view: View) {}

	@CallSuper
	fun bind(viewModel: M, payload: Bundle?) {
		this.viewModel = viewModel
		payload?.let { bindPayloads(it) } ?: bind(viewModel)
	}

	/**
	 * Полное обновление данных, в том числе через databinding
	 *
	 * @param viewModel ViewModel
	 */
	protected open fun bind(viewModel: M) {}

	/**
	 * Частичное обновление данных
	 *
	 * @param payload Bundle с данными требующимии обновления на view'шке
	 */
	protected open fun bindPayloads(payload: Bundle) {}

	@JvmOverloads
	fun onClick(action: Int, extra: Bundle = Bundle()) {
		val position = adapterPosition
		adapterWeakReference?.get()?.let {
			if (position != NO_POSITION) {
				it.onClick(position, viewModel, action, extra)
			}
		}
	}

	/**
	 * RecyclerView calls this method right before clearing ViewHolder's internal data and
	 * sending it to RecycledViewPool
	 */
	open fun onUnbind() {}
}

