package ru.starksoft.differ.sample.screens.sample.adapter.viewholder

import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_header.view.*
import ru.starksoft.differ.adapter.OnClickListener
import ru.starksoft.differ.sample.R
import ru.starksoft.differ.sample.screens.sample.adapter.viewmodel.HeaderViewModel
import ru.starksoft.differ.viewholder.DifferViewHolder

class HeaderViewHolder(
		parent: ViewGroup,
		onClickListener: OnClickListener?
) : DifferViewHolder<HeaderViewModel>(R.layout.item_header, parent, onClickListener) {

	override fun bind(viewModel: HeaderViewModel) {
		itemView.text.text = viewModel.text
	}
}
