package ru.starksoft.differ.sample.screens.sample.adapter.viewmodel

import ru.starksoft.differ.adapter.viewmodel.DifferViewModel
import ru.starksoft.differ.divider.DividerType

data class SampleViewModel(
	val hashCode: Int,
	val id: Int,
	val text: String,
	val image: String,
	val divider: DividerType,
	val needScrollTo: Boolean
) : DifferViewModel(hashCode) {

	override fun getItemHashCode(): Int {
		return super.getItemHashCode(id)
	}

	override fun getDividerType(): DividerType {
		return divider
	}

	override fun needScrollTo(): Boolean {
		return needScrollTo
	}
}
