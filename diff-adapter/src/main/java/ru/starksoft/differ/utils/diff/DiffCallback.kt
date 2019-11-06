package ru.starksoft.differ.utils.diff

import androidx.recyclerview.widget.DiffUtil
import ru.starksoft.differ.viewmodel.ViewModel

class DiffCallback<M : ViewModel>(private val oldList: List<M>, private val newList: List<M>) : DiffUtil.Callback() {

	override fun getOldListSize(): Int {
		return oldList.size
	}

	override fun getNewListSize(): Int {
		return newList.size
	}

	override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
		val oldItem = oldList[oldItemPosition]
		val newItem = newList[newItemPosition]
		return oldItem.getItemHashCode() == newItem.getItemHashCode()
	}

	override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
		val oldItem = oldList[oldItemPosition]
		val newItem = newList[newItemPosition]
		return oldItem == newItem
	}

	override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
		val oldItem = oldList[oldItemPosition]
		val newItem = newList[newItemPosition]
		return oldItem.getChangePayload(newItem)
	}
}
