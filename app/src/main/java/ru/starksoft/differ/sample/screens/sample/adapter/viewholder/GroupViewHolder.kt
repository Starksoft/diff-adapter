package ru.starksoft.differ.sample.screens.sample.adapter.viewholder

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import ru.starksoft.differ.adapter.OnClickListener
import ru.starksoft.differ.adapter.viewholder.ClickAction
import ru.starksoft.differ.adapter.viewholder.DifferViewHolder
import ru.starksoft.differ.sample.R
import ru.starksoft.differ.sample.screens.sample.adapter.viewmodel.GroupViewModel

@Keep
class GroupViewHolder(parent: ViewGroup, onClickListener: OnClickListener?) :
    DifferViewHolder<GroupViewModel>(R.layout.item_group, parent, onClickListener) {

    private val groupTextView by lazy { itemView.findViewById<TextView>(R.id.groupTextView) }
    private val groupImageView by lazy { itemView.findViewById<ImageView>(R.id.groupImageView) }

    override fun bind(viewModel: GroupViewModel) {
        super.bind(viewModel)

        itemView.setOnClickListener {
            onClick(ACTION_GROUP)
        }

        groupTextView.text = viewModel.title
        groupImageView.setImageResource(if (viewModel.expanded) R.drawable.ic_arrow_short_top else R.drawable.ic_arrow_short_bottom)

    }

    companion object {

        val ACTION_GROUP = ClickAction[this::class.java, "ACTION_GROUP"]
    }
}