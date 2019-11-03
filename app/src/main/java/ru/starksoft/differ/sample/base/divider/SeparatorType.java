package ru.starksoft.differ.sample.base.divider;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
		SeparatorType.SEPARATOR_DISABLED,
		SeparatorType.SEPARATOR_LEFT_PADDING_16,
		SeparatorType.SEPARATOR_PADDING_16,
		SeparatorType.SEPARATOR_WITHOUT_PADDING,
		SeparatorType.SEPARATOR_LEFT_PADDING_48,
		SeparatorType.SEPARATOR_TOP})
@Retention(RetentionPolicy.SOURCE)
public @interface SeparatorType {
	int SEPARATOR_DISABLED = 0;
	int SEPARATOR_LEFT_PADDING_16 = 1;
	int SEPARATOR_PADDING_16 = 2;
	int SEPARATOR_WITHOUT_PADDING = 3;
	int SEPARATOR_LEFT_PADDING_48 = 4;
	int SEPARATOR_TOP = 5;
}
