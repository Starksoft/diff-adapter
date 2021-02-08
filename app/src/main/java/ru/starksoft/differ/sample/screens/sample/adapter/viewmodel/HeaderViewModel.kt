package ru.starksoft.differ.sample.screens.sample.adapter.viewmodel

import ru.starksoft.differ.adapter.viewmodel.DifferViewModel
import ru.starksoft.differ.divider.DividerType

data class HeaderViewModel(val hashCode: Int, val text: String) : DifferViewModel(hashCode) {

    override fun getItemHashCode(): Int {
        return super.getItemHashCode(text)
    }

    override fun getDividerType(): DividerType {
        return DividerType.DISABLED
    }
}
