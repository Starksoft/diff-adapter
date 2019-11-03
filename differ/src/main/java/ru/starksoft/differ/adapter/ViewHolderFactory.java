package ru.starksoft.differ.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import ru.starksoft.differ.viewholder.DifferViewHolder;

public interface ViewHolderFactory {
	@NonNull
	DifferViewHolder create(@NonNull ViewGroup parent, int viewType, @NonNull OnClickListener onClickListener);
}
