package ru.starksoft.differ.mutable

abstract class ObjectMutableParam<T> : BaseMutableParam() {

	var param: T? = null

	override fun toString(): String {
		return "ObjectMutableParam{param=$param}"
	}
}
