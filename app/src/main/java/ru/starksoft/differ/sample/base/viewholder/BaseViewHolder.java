package ru.starksoft.differ.sample.base.viewholder;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.starksoft.differ.adapter.OnClickListener;
import ru.starksoft.differ.viewholder.DifferViewHolder;
import ru.starksoft.differ.viewmodel.ViewModel;

public abstract class BaseViewHolder <M extends ViewModel> extends DifferViewHolder<M> {

	public BaseViewHolder(int layout, @NonNull ViewGroup parent, @Nullable OnClickListener onClickListener) {
		super(layout, parent, onClickListener);
	}

	@Override
	protected void setBinder(@NonNull View view) {
//		ButterKnife.bind(this, itemView);
	}
}
