package ru.starksoft.differ.sample.base.viewmodel.mutable;

import org.jetbrains.annotations.NotNull;

abstract class BaseMutableParam {

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException("You can't use get() for mutable param!");
	}

	@NotNull
	@Override
	public String toString() {
		return "BaseMutableParam{}";
	}

}
