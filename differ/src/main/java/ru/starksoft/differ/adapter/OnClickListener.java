package ru.starksoft.differ.adapter;

import android.os.Bundle;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import ru.starksoft.differ.viewmodel.ViewModel;

public interface OnClickListener {

	boolean onClick(@IntRange(from = 0) int position, @NonNull ViewModel viewModel, int action, @NonNull Bundle extra);

}
