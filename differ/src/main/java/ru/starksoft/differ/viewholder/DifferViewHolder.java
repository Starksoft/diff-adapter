package ru.starksoft.differ.viewholder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import ru.starksoft.differ.adapter.OnClickListener;
import ru.starksoft.differ.viewmodel.ViewModel;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public abstract class DifferViewHolder <M extends ViewModel> extends RecyclerView.ViewHolder {

	@Nullable private final WeakReference<OnClickListener> adapterWeakReference;
	private M viewModel;

	public DifferViewHolder(@LayoutRes int layout, @NonNull ViewGroup parent, @Nullable OnClickListener onClickListener) {
		super(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
		setBinder(itemView);
		if (onClickListener != null) {
			adapterWeakReference = new WeakReference<>(onClickListener);
		} else {
			adapterWeakReference = null;
		}
	}

	protected abstract void setBinder(@NonNull View view);

	@CallSuper
	public void bind(@NonNull M viewModel, @Nullable Bundle diff) {
		this.viewModel = viewModel;
		if (diff != null) {
			bindPayloads(diff);
		} else {
			bind(viewModel);
		}
	}

	/**
	 * Полное обновление данных, в том числе через databinding
	 *
	 * @param viewModel ViewModel
	 */
	protected void bind(@NonNull M viewModel) {
	}

	/**
	 * Частичное обновление данных
	 *
	 * @param diff Bundle с данными требующимии обновления на view'шке
	 */
	protected void bindPayloads(@NonNull Bundle diff) {
	}

	@NonNull
	public M getViewModel() {
		return viewModel;
	}

	public void onClick(int action) {
		onClick(action, new Bundle());
	}

	public void onClick(int action, @NonNull Bundle extra) {
		if (adapterWeakReference != null) {
			OnClickListener listener = adapterWeakReference.get();
			if (listener != null) {
				int position = getAdapterPosition();
				if (position != NO_POSITION) {
					listener.onClick(getAdapterPosition(), getViewModel(), action, extra);
				}
			}
		}

	}

	/**
	 * RecyclerView calls this method right before clearing ViewHolder's internal data and
	 * sending it to RecycledViewPool
	 */
	public void onUnbind() {
	}
}
