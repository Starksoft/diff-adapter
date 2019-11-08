package ru.starksoft.differ.api.mutable

class IntegerMutableParam : BaseMutableParam() {

	var param: Int = 0

	override fun toString(): String {
		return "IntegerMutableParam{param=$param}"
	}
}
