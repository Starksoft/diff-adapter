package ru.starksoft.differ.sample.screens.sample.adapter.viewmodel

import ru.starksoft.differ.divider.DividerType
import ru.starksoft.differ.adapter.viewmodel.DifferViewModel

data class SampleViewModel(val hashCode: Int, val id: Int, val text: String, val image: String) : DifferViewModel(hashCode) {

	override fun getItemHashCode(): Int {
		return super.getItemHashCode(id)
	}

	override fun getDividerType(): DividerType {
		return DividerType.PADDING_16
	}
}
