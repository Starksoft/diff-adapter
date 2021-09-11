package ru.starksoft.differ.adapter

import android.view.ViewGroup
import ru.starksoft.differ.adapter.viewholder.DifferViewHolder
import ru.starksoft.differ.adapter.viewmodel.DifferViewModel

fun interface ViewHolderFactory {

    fun create(
        parent: ViewGroup,
        viewType: Int,
        onClickListener: OnClickListener?
    ): DifferViewHolder<out DifferViewModel>
}