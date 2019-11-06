package ru.starksoft.differ.mutable

class StringMutableParam : ObjectMutableParam<String>() {

	override fun toString(): String {
		return "StringMutableParam() ${super.toString()}"
	}
}
