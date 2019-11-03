package ru.starksoft.differ.sample.screens.sample.adapter.viewholder

import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_text.view.*
import ru.starksoft.differ.adapter.OnClickListener
import ru.starksoft.differ.sample.R
import ru.starksoft.differ.sample.base.viewholder.BaseViewHolder
import ru.starksoft.differ.sample.screens.sample.adapter.viewmodel.TextViewModel

class TextViewHolder(
		parent: ViewGroup,
		onClickListener: OnClickListener?
) : BaseViewHolder<TextViewModel>(R.layout.item_text, parent, onClickListener) {

	override fun bind(viewModel: TextViewModel) {
		itemView.text.text = viewModel.text
	}
}
