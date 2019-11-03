package ru.starksoft.differ.sample.base.viewmodel.mutable;

import org.jetbrains.annotations.NotNull;

public final class StringMutableParam extends ObjectMutableParam<String> {
	@NotNull
	@Override
	public String toString() {
		return "StringMutableParam{}";
	}
}
