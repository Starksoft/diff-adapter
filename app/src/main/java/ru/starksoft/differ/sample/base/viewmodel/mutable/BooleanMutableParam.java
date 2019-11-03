package ru.starksoft.differ.sample.base.viewmodel.mutable;

import org.jetbrains.annotations.NotNull;

public final class BooleanMutableParam extends BaseMutableParam {

	private boolean param;

	public boolean getParam() {
		return param;
	}

	public void setParam(boolean param) {
		this.param = param;
	}

	@NotNull
	@Override
	public String toString() {
		return "BooleanMutableParam{" + "param=" + param + '}';
	}
}
