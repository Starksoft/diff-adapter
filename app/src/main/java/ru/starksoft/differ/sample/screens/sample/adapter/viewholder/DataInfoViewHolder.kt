package ru.starksoft.differ.sample.screens.sample.adapter.viewholder

import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_data_info.view.*
import ru.starksoft.differ.adapter.OnClickListener
import ru.starksoft.differ.adapter.viewholder.DifferViewHolder
import ru.starksoft.differ.sample.R
import ru.starksoft.differ.sample.screens.sample.adapter.viewmodel.DataInfoViewModel

class DataInfoViewHolder(
    parent: ViewGroup,
    onClickListener: OnClickListener?
) : DifferViewHolder<DataInfoViewModel>(R.layout.item_data_info, parent, onClickListener) {

    override fun bind(viewModel: DataInfoViewModel) {
        itemView.text.text = viewModel.text
    }
}
