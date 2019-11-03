package ru.starksoft.differ.sample.base.viewmodel.mutable;

import androidx.annotation.Nullable;

abstract class ObjectMutableParam <T> extends BaseMutableParam {

	@Nullable private T param;

	@Nullable
	public T getParam() {
		return param;
	}

	public void setParam(@Nullable T param) {
		this.param = param;
	}
}
