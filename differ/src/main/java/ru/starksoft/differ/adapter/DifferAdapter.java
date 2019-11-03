package ru.starksoft.differ.adapter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import androidx.annotation.AnyThread;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;
import ru.starksoft.differ.Logger;
import ru.starksoft.differ.utils.ExecutorHelper;
import ru.starksoft.differ.utils.diff.DiffCallback;
import ru.starksoft.differ.viewholder.DifferViewHolder;
import ru.starksoft.differ.viewmodel.DifferViewModel;
import ru.starksoft.differ.viewmodel.ViewModel;
import ru.starksoft.differ.viewmodel.ViewModelReused;

public abstract class DifferAdapter extends RecyclerView.Adapter<DifferViewHolder> implements OnClickListener {

	public static final String TAG = "DifferAdapter";
	private static final String LOG_CALLBACK_TEMPLATE = "[ADAPTER] %1$s position = %2$s count = %3$s | %4$s";
	private static final String LOG_UPDATE_TEMPLATE = "Adapter::%1$s %2$s %3$s";

	@NonNull protected final OnClickListener onClickListener;
	protected final List<ViewModel> data = new ArrayList<>();
	private final Deque<DifferRefreshingData> pendingUpdates = new ArrayDeque<>();
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final List<OnCompletedUpdateAdapterListener> listOnCompletedUpdateAdapterListener = new ArrayList<>();
	private final ViewHolderFactory viewHolderFactory;
	@NonNull private final Logger logger;
	private ExecutorHelper executor = ExecutorHelper.newSingleThreadExecutor();

	public DifferAdapter(@NonNull OnClickListener onClickListener, @NonNull Logger logger) {
		this(onClickListener, Collections.emptyList(), logger);
	}

	public DifferAdapter(@NonNull OnClickListener onClickListener, @NonNull ViewModel[] array, @NonNull Logger logger) {
		this(onClickListener, Arrays.asList(array), logger);
	}

	public DifferAdapter(@NonNull OnClickListener onClickListener, @NonNull List<ViewModel> list, @NonNull Logger logger) {
		this.logger = logger;
		data.addAll(list);
		this.viewHolderFactory = newInstanceViewHolderFactory();
		this.onClickListener = onClickListener;
	}

	@NonNull
	protected abstract ViewHolderFactory newInstanceViewHolderFactory();

	@NonNull
	@Override
	public DifferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return viewHolderFactory.create(parent, viewType, this);
	}

	@Override
	public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		if (executor.isShutdown()) {
			executor = ExecutorHelper.newSingleThreadExecutor();
		}
	}

	@Override
	public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
		executor.destroy();
		pendingUpdates.clear();
		handler.removeCallbacksAndMessages(null);
		super.onDetachedFromRecyclerView(recyclerView);
	}

	public void setMaxRecycledViews(@NonNull RecyclerView recyclerView) {
	}

	@SafeVarargs
	public final void setMaxRecycledViews(@NonNull RecyclerView recyclerView,
										  @NonNull Pair<Class<? extends ViewModel>, Integer>... maxRecycledViews) {
		RecyclerView.RecycledViewPool viewPool = recyclerView.getRecycledViewPool();
		for (Pair<Class<? extends ViewModel>, Integer> pair : maxRecycledViews) {
			Class<? extends ViewModel> first = pair.first;
			Integer second = pair.second;

			if (first == null || second == null) {
				throw new IllegalStateException("Params of pair is null");
			}
			viewPool.setMaxRecycledViews(DifferViewModel.getItemViewType(first), second);
		}
	}

	public void addOnCompletedUpdateAdapterListener(@NonNull OnCompletedUpdateAdapterListener onCompletedUpdateAdapterListener) {
		listOnCompletedUpdateAdapterListener.add(onCompletedUpdateAdapterListener);
	}

	public void removeAllOnCompletedUpdateAdapterListener() {
		listOnCompletedUpdateAdapterListener.clear();
	}

	@Nullable
	public final ViewModel getItem(@IntRange(from = 0) int position) {
		return data.get(position);
	}

	@NonNull
	protected ViewModel getLastItem() {
		return data.get(getItemCount() - 1);
	}

	public int getPosition(@Nullable ViewModel item) {
		return data.indexOf(item);
	}

	@Override
	public void onBindViewHolder(@NonNull DifferViewHolder holder, int position) {
		ViewModel item = getItem(position);
		if (item != null) {
			//noinspection unchecked
			holder.bind(item, null);
		}
	}

	@Override
	public void onBindViewHolder(@NonNull DifferViewHolder holder, int position, @NonNull List<Object> payloads) {
		ViewModel item = getItem(position);
		if (item != null) {
			//noinspection unchecked
			holder.bind(item, getPayloads(payloads));
		}
	}

	@Nullable
	private Bundle getPayloads(@NonNull List<Object> payloads) {
		if (payloads.isEmpty()) {
			return null;
		}

		Object object = payloads.get(0);
		if (!(object instanceof Bundle)) {
			return null;
		}

		return (Bundle) object;
	}

	@Override
	public int getItemViewType(int position) {
		ViewModel item = getItem(position);
		return item != null ? item.getItemViewType() : super.getItemViewType(position);
	}

	@Override
	@IntRange(from = 0)
	public final int getItemCount() {
		return data.size();
	}

	public final boolean isEmpty() {
		return getItemCount() < 1;
	}

	private void notifyOnAdapterUpdatedListeners(@NonNull DifferLabels viewModelLabels) {
		for (OnCompletedUpdateAdapterListener listener : listOnCompletedUpdateAdapterListener) {
			listener.completedUpdateAdapter(viewModelLabels);
		}
	}

	@UiThread
	public final void update(@NonNull List<ViewModel> viewModels, @NonNull DifferLabels labels) {
		DifferRefreshingData differRefreshingData = DifferRefreshingData.obtain();
		differRefreshingData.init(viewModels, labels);
		labels.release();

		logger.d(TAG, String.format(LOG_UPDATE_TEMPLATE, getClass().getSimpleName(), "update", differRefreshingData.getLabels().log()));
		pendingUpdates.add(differRefreshingData);
		if (pendingUpdates.size() == 1) {
			internalUpdate(differRefreshingData);
		}
	}

	@AnyThread
	private void internalUpdate(@NonNull DifferRefreshingData differRefreshingData) {
		executor.submit(() -> {
			logger.d(TAG,
					 String.format(LOG_UPDATE_TEMPLATE,
								   getClass().getSimpleName(),
								   "internalUpdate",
								   differRefreshingData.getLabels().log()));
			DiffUtil.DiffResult diffResult =
					DiffUtil.calculateDiff(new DiffCallback<>(new ArrayList<>(data), differRefreshingData.getItems()), false);
			handler.post(() -> {
				dispatchUpdates(differRefreshingData, diffResult);
				processQueue();
			});
		});
	}

	@UiThread
	private void processQueue() {
		if (!pendingUpdates.isEmpty()) {
			pendingUpdates.remove();
			if (!pendingUpdates.isEmpty()) {
				if (pendingUpdates.size() > 1) {
					DifferRefreshingData last = pendingUpdates.pollLast();
					while (!pendingUpdates.isEmpty()) {
						pendingUpdates.pop().release();
					}
					pendingUpdates.add(last);
				}
				internalUpdate(pendingUpdates.peek());
			}
		}
	}

	@UiThread
	private void dispatchUpdates(@NonNull DifferRefreshingData differRefreshingData, @NonNull DiffUtil.DiffResult diffResult) {
		data.clear();
		data.addAll(differRefreshingData.getItems());

		diffResult.dispatchUpdatesTo(createListUpdateCallback());

		notifyOnAdapterUpdatedListeners(differRefreshingData.getLabels());

		logger.d(TAG, "Adapter::" + getClass().getSimpleName() + " DRAW " + differRefreshingData.getLabels().log());

		differRefreshingData.release();
	}

	@NonNull
	private ListUpdateCallback createListUpdateCallback() {
		return new ListUpdateCallback() {
			@Override
			public void onInserted(int position, int count) {
				logger.w(TAG, String.format(LOG_CALLBACK_TEMPLATE, "onInserted", position, count, DifferAdapter.this));
				notifyItemRangeInserted(position, count);
			}

			@Override
			public void onRemoved(int position, int count) {
				logger.w(TAG, String.format(LOG_CALLBACK_TEMPLATE, "onRemoved", position, count, DifferAdapter.this));
				notifyItemRangeRemoved(position, count);
			}

			@Override
			public void onMoved(int fromPosition, int toPosition) {
				logger.w(TAG,
						 "[ADAPTER] onMoved fromPosition = " + fromPosition + " toPosition = " + toPosition + " | " + DifferAdapter.this);
				notifyItemMoved(fromPosition, toPosition);
			}

			@Override
			public void onChanged(int position, int count, Object payload) {
				logger.w(TAG,
						 "[ADAPTER] onChanged position = " + position + " count = " + count + " payload = " + payload + " | " +
								 DifferAdapter.this);
				notifyItemRangeChanged(position, count, payload);
			}
		};
	}

	@Override
	public final boolean onClick(@IntRange(from = 0) int position, @NonNull ViewModel viewModel, int action, @NonNull Bundle extra) {
		ViewModel item = getItem(position);
		return item != null && onClickListener.onClick(position, item, action, extra);
	}

	@Override
	public void onViewRecycled(@NonNull DifferViewHolder holder) {
		super.onViewRecycled(holder);
		holder.onUnbind();
	}

	@FunctionalInterface
	public interface OnBuildAdapterListener {
		/**
		 * Собирает список ViewModel'ей
		 */
		@WorkerThread
		void buildViewModelList(@NonNull ViewModelReused viewModelReused);
	}

	@FunctionalInterface
	public interface OnRefreshAdapterListener {
		/**
		 * Пересобирает адаптер вызывая buildViewModelList
		 */
		@AnyThread
		void refreshAdapter(@Nullable int... labels);
	}

	@FunctionalInterface
	public interface OnCompletedUpdateAdapterListener {
		/**
		 * Вызывается после окончания обновления адаптера
		 */
		@UiThread
		void completedUpdateAdapter(@NonNull DifferLabels viewModelLabels);
	}

}
