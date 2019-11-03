package ru.starksoft.differ.sample.screens.sample.adapter;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import ru.starksoft.differ.sample.base.presenter.BaseAdapterPresenter;
import ru.starksoft.differ.sample.base.presenter.OnAdapterRefreshedListener;
import ru.starksoft.differ.sample.screens.sample.adapter.viewmodel.TextViewModel;
import ru.starksoft.differ.viewmodel.ViewModel;
import ru.starksoft.differ.viewmodel.ViewModelReused;

public class SampleAdapterDelegate extends BaseAdapterPresenter {

	private final List<String> data = new ArrayList<>();

	public SampleAdapterDelegate(@NonNull OnAdapterRefreshedListener onAdapterRefreshedListener) {
		super(onAdapterRefreshedListener);
	}

	@Override
	public void buildViewModelList(@NonNull ViewModelReused viewModelReused) {
		for (String datum : data) {
			viewModelReused.add(new TextViewModel(0, datum));
		}
	}

	@Override
	public boolean onClick(int position, @NonNull ViewModel viewModel, int action, @NonNull Bundle extra) {
		return false;
	}

	public void populate(int count) {
		for (int i = 0; i < count; i++) {
			data.add("String #" + (i + 1));
		}
	}
}
