package ru.starksoft.differ.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import ru.starksoft.differ.adapter.viewholder.DifferViewHolder;

@FunctionalInterface
public interface ViewHolderFactory {

    @NonNull
    DifferViewHolder create(ViewGroup parent, int viewType, OnClickListener onClickListener);
}
