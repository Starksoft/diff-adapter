package ru.starksoft.differ.sample.base.viewmodel.mutable;

import org.jetbrains.annotations.NotNull;

public final class IntegerMutableParam extends BaseMutableParam {

	private int param;

	public int getParam() {
		return param;
	}

	public void setParam(int param) {
		this.param = param;
	}

	@NotNull
	@Override
	public String toString() {
		return "IntegerMutableParam{" + "param=" + param + '}';
	}
}
