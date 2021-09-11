package ru.starksoft.differ.sample.screens.sample.adapter.viewmodel

import ru.starksoft.differ.adapter.viewmodel.DifferViewModel

data class GroupViewModel(
    val hashCode: Int,
    val title: String,
    val expanded: Boolean
) : DifferViewModel(hashCode) {

    override fun getItemHashCode(): Int {
        return super.getItemHashCode(title)
    }
}