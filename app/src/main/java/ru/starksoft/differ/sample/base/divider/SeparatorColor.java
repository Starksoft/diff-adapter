package ru.starksoft.differ.sample.base.divider;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({SeparatorColor.DARK, SeparatorColor.LIGHT})
public @interface SeparatorColor {
	int LIGHT = 1;
	int DARK = 2;
}
