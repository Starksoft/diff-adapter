package ru.starksoft.differ.sample.base.viewmodel.mutable;

import org.jetbrains.annotations.NotNull;

public final class LongMutableParam extends BaseMutableParam {

	private long param;

	public long getParam() {
		return param;
	}

	public void setParam(long param) {
		this.param = param;
	}

	@NotNull
	@Override
	public String toString() {
		return "LongMutableParam{" + "param=" + param + '}';
	}
}
