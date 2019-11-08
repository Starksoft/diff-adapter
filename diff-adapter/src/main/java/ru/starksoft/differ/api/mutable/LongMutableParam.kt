package ru.starksoft.differ.api.mutable

class LongMutableParam : BaseMutableParam() {

	var param: Long = 0

	override fun toString(): String {
		return "LongMutableParam{param=$param}"
	}
}
