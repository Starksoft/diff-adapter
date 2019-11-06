package ru.starksoft.differ.mutable

@Suppress("EqualsOrHashCode")
abstract class BaseMutableParam {

	override fun hashCode(): Int {
		throw UnsupportedOperationException("You can't use get() for mutable param!")
	}

	override fun toString(): String {
		return "BaseMutableParam{}"
	}
}
