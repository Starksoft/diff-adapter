package ru.starksoft.differ.utils.diff;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import ru.starksoft.differ.viewmodel.ViewModel;

public class DiffCallback <M extends ViewModel> extends DiffUtil.Callback {

	@NonNull private final List<M> oldList;
	@NonNull private final List<M> newList;

	public DiffCallback(@NonNull List<M> oldList, @NonNull List<M> newList) {
		this.oldList = oldList;
		this.newList = newList;
	}

	@Override
	public int getOldListSize() {
		return oldList.size();
	}

	@Override
	public int getNewListSize() {
		return newList.size();
	}

	@Override
	public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
		M oldItem = oldList.get(oldItemPosition);
		M newItem = newList.get(newItemPosition);
		return oldItem.getItemHashCode() == newItem.getItemHashCode();
	}

	@Override
	public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
		M oldItem = oldList.get(oldItemPosition);
		M newItem = newList.get(newItemPosition);
		return oldItem.equals(newItem);
	}

	@Nullable
	@Override
	public Object getChangePayload(int oldItemPosition, int newItemPosition) {
		M oldItem = oldList.get(oldItemPosition);
		M newItem = newList.get(newItemPosition);
		return oldItem.getChangePayload(newItem);
	}

}
