package ru.starksoft.differ.sample.screens.sample.adapter

import ru.starksoft.differ.adapter.OnClickListener
import ru.starksoft.differ.adapter.ViewHolderFactory
import ru.starksoft.differ.sample.base.adapter.BaseAdapter
import ru.starksoft.differ.sample.base.viewmodel.BaseViewModel
import ru.starksoft.differ.sample.screens.sample.adapter.viewholder.TextViewHolder
import ru.starksoft.differ.sample.screens.sample.adapter.viewmodel.TextViewModel

class SampleAdapter(onClickListener: OnClickListener) : BaseAdapter(onClickListener) {

	override fun newInstanceViewHolderFactory(): ViewHolderFactory {
		return ViewHolderFactory { parent, viewType, onClickListener ->
			return@ViewHolderFactory if (BaseViewModel.getItemViewType(TextViewModel::class.java) == viewType) {
				TextViewHolder(parent, onClickListener)
			} else {
				throw IllegalStateException()
			}
		}
	}
}
