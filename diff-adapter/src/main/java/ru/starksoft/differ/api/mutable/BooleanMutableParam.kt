package ru.starksoft.differ.api.mutable

class BooleanMutableParam : BaseMutableParam() {

	var param: Boolean = false

	override fun toString(): String {
		return "BooleanMutableParam{param=$param}"
	}
}
