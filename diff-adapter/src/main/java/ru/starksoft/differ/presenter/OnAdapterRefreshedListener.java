package ru.starksoft.differ.presenter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import ru.starksoft.differ.adapter.DifferLabels;
import ru.starksoft.differ.viewmodel.ViewModel;

public interface OnAdapterRefreshedListener {
	@UiThread
	void updateAdapter(@NonNull List<ViewModel> viewModels, @NonNull DifferLabels labels);

}
