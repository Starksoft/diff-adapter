package ru.starksoft.differ.sample.screens.sample.adapter.viewmodel

import ru.starksoft.differ.adapter.viewmodel.DifferViewModel
import ru.starksoft.differ.adapter.viewmodel.ViewModel
import ru.starksoft.differ.divider.DividerType
import ru.starksoft.differ.utils.hash.HashCode

data class SampleViewModel(
    val hashCode: Int,
    val id: Int,
    val text: String,
    val image: String,
    val divider: DividerType,
    val needScrollTo: Boolean
) : DifferViewModel(hashCode) {

    override fun getItemHashCode() = super.getItemHashCode(id)

    override fun getContentHashCode() = HashCode[hashCode, text, image]

    override fun getDividerType() = divider

    override fun needScrollTo() = needScrollTo

    override fun scrollStrategy() = ViewModel.ScrollStrategy.CENTER
}
