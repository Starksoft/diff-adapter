package ru.starksoft.differ.sample.screens.sample.adapter.viewmodel

import ru.starksoft.differ.adapter.viewmodel.DifferViewModel
import ru.starksoft.differ.divider.DividerType

data class DataInfoViewModel(val hashCode: Int, val id: Int, val text: String) : DifferViewModel(hashCode) {

    override fun getItemHashCode() = super.getItemHashCode(id)

    override fun getDividerType() = DividerType.DISABLED
}
