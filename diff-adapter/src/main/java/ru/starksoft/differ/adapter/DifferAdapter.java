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
import ru.starksoft.differ.divider.DividerType;
import ru.starksoft.differ.utils.ExecutorHelper;
import ru.starksoft.differ.utils.ThreadUtils;
import ru.starksoft.differ.utils.diff.DiffCallback;
import ru.starksoft.differ.viewholder.DifferViewHolder;
import ru.starksoft.differ.viewmodel.DifferViewModel;
import ru.starksoft.differ.viewmodel.ViewModel;
import ru.starksoft.differ.viewmodel.ViewModelReused;

public abstract class DifferAdapter extends RecyclerView.Adapter<DifferViewHolder> implements OnClickListener {

	private static final String TAG = "DifferAdapter";
	private static final String LOG_CALLBACK_TEMPLATE = "[ADAPTER] %1$s position = %2$s count = %3$s | %4$s";
	private static final String LOG_UPDATE_TEMPLATE = "Adapter::%1$s %2$s %3$s";

	@NonNull private final OnClickListener onClickListener;
	private final List<ViewModel> data = new ArrayList<>();
	private final Deque<DifferRefreshingData> pendingUpdates = new ArrayDeque<>();
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final List<OnCompletedUpdateAdapterListener> listOnCompletedUpdateAdapterListener = new ArrayList<>();
	private final ViewHolderFactory viewHolderFactory;
	@Nullable private final Logger logger;
	private final ExecutorHelper executorHelper;
	@Nullable private DifferAdapterEventListener eventListener;

	public DifferAdapter(@NonNull ViewHolderFactory viewHolderFactory, @NonNull OnClickListener onClickListener, @Nullable Logger logger,
						 ExecutorHelper executorHelper) {
		this(viewHolderFactory, onClickListener, Collections.emptyList(), logger, executorHelper);
	}

	public DifferAdapter(@NonNull ViewHolderFactory viewHolderFactory, @NonNull OnClickListener onClickListener, @NonNull ViewModel[] array,
						 @Nullable Logger logger, ExecutorHelper executorHelper) {
		this(viewHolderFactory, onClickListener, Arrays.asList(array), logger, executorHelper);
	}

	public DifferAdapter(@NonNull ViewHolderFactory viewHolderFactory, @NonNull OnClickListener onClickListener,
						 @NonNull List<ViewModel> list, @Nullable Logger logger, ExecutorHelper executorHelper) {
		this.logger = logger;
		this.executorHelper = executorHelper;
		data.addAll(list);
		this.viewHolderFactory = viewHolderFactory;
		this.onClickListener = onClickListener;
	}

	@NonNull
	public static String getElementInfo(@Nullable List<? extends ViewModel> list, int position) {
		synchronized (DifferAdapter.class) {
			StringBuilder stringBuilder = new StringBuilder();
			final boolean empty = list == null || list.isEmpty();
			stringBuilder.append("\n listSize = ").append("(").append(empty ? "null" : list.size()).append(")");

			if (!empty) {
				final ViewModel item = list.size() > position ? list.get(position) : null;
				String itemContent = item != null ? item.toString() : "null";
				stringBuilder.append(" item").append("(").append(position).append(")").append(" = ").append(itemContent);
			}
			return stringBuilder.toString();
		}
	}

	public void setEventListener(@NonNull DifferAdapterEventListener eventListener) {
		this.eventListener = eventListener;
	}

	@NonNull
	@Override
	public DifferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return viewHolderFactory.create(parent, viewType, this);
	}

	@Override
	public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
	}

	@Override
	public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
		executorHelper.destroy();
		pendingUpdates.clear();
		handler.removeCallbacksAndMessages(null);
		super.onDetachedFromRecyclerView(recyclerView);
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
		synchronized (DifferAdapter.class) {
			listOnCompletedUpdateAdapterListener.add(onCompletedUpdateAdapterListener);
		}
	}

	public void removeAllOnCompletedUpdateAdapterListener() {
		synchronized (DifferAdapter.class) {
			listOnCompletedUpdateAdapterListener.clear();
		}
	}

	@Nullable
	private ViewModel getItem(@IntRange(from = 0) int position) {
		synchronized (DifferAdapter.class) {
			return data.get(position);
		}
	}

	@NonNull
	private ViewModel getLastItem() {
		synchronized (DifferAdapter.class) {
			return data.get(getItemCount() - 1);
		}
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
		synchronized (DifferAdapter.class) {
			DifferRefreshingData differRefreshingData = DifferRefreshingData.obtain();
			differRefreshingData.init(viewModels, labels);
			labels.release();

			if (logger != null) {
				logger.d(TAG,
						 String.format(LOG_UPDATE_TEMPLATE, getClass().getSimpleName(), "update", differRefreshingData.getLabels().log()));
			}
			pendingUpdates.add(differRefreshingData);
			if (pendingUpdates.size() == 1) {
				internalUpdate(differRefreshingData);
			}
		}
	}

	@AnyThread
	private void internalUpdate(@NonNull DifferRefreshingData differRefreshingData) {
		executorHelper.submit(() -> {
			synchronized (DifferAdapter.class) {
				if (logger != null) {
					logger.d(TAG,
							 String.format(LOG_UPDATE_TEMPLATE,
										   getClass().getSimpleName(),
										   "internalUpdate",
										   differRefreshingData.getLabels().log()));
				}
				DiffUtil.DiffResult diffResult =
						DiffUtil.calculateDiff(new DiffCallback<>(new ArrayList<>(data), differRefreshingData.getItems()), false);

				handler.post(() -> {
					synchronized (DifferAdapter.class) {
						dispatchUpdates(differRefreshingData, diffResult);
						processQueue();
					}
				});
			}
		});
	}

	@UiThread
	private void processQueue() {
		ThreadUtils.checkMainThread();
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
				DifferRefreshingData differRefreshingData = pendingUpdates.peek();
				if (differRefreshingData != null) {
					internalUpdate(differRefreshingData);
				}
			}
		}
	}

	@UiThread
	private void dispatchUpdates(@NonNull DifferRefreshingData differRefreshingData, @NonNull DiffUtil.DiffResult diffResult) {
		ThreadUtils.checkMainThread();
		data.clear();
		data.addAll(differRefreshingData.getItems());

		diffResult.dispatchUpdatesTo(createListUpdateCallback());

		notifyOnAdapterUpdatedListeners(differRefreshingData.getLabels());

		if (eventListener != null) {
			eventListener.onFinished(differRefreshingData.getItems());
		}

		if (logger != null) {
			logger.d(TAG, "Adapter::" + getClass().getSimpleName() + " DRAW " + differRefreshingData.getLabels().log());
		}

		differRefreshingData.release();
	}

	@NonNull
	private ListUpdateCallback createListUpdateCallback() {
		return new ListUpdateCallback() {
			@Override
			public void onInserted(int position, int count) {
				if (logger != null) {
					logger.d(TAG, String.format(LOG_CALLBACK_TEMPLATE, "onInserted", position, count, DifferAdapter.this));
				}
				notifyItemRangeInserted(position, count);
				if (eventListener != null) {
					eventListener.onInserted(position, count);
				}
			}

			@Override
			public void onRemoved(int position, int count) {
				if (logger != null) {
					logger.d(TAG, String.format(LOG_CALLBACK_TEMPLATE, "onRemoved", position, count, DifferAdapter.this));
				}
				notifyItemRangeRemoved(position, count);
				if (eventListener != null) {
					eventListener.onRemoved(position, count);
				}
			}

			@Override
			public void onMoved(int fromPosition, int toPosition) {
				if (logger != null) {
					logger.d(TAG,
							 "[ADAPTER] onMoved fromPosition = " + fromPosition + " toPosition = " + toPosition + " | " +
									 DifferAdapter.this);
				}
				notifyItemMoved(fromPosition, toPosition);
				if (eventListener != null) {
					eventListener.onMoved(fromPosition, toPosition);
				}
			}

			@Override
			public void onChanged(int position, int count, Object payload) {
				if (logger != null) {
					logger.d(TAG,
							 "[ADAPTER] onChanged position = " + position + " count = " + count + " payload = " + payload + " | " +
									 DifferAdapter.this);
				}
				notifyItemRangeChanged(position, count, payload);
				if (eventListener != null) {
					eventListener.onChanged(position, count, payload);
				}
			}
		};
	}

	@Override
	public final boolean onClick(@IntRange(from = 0) int position, @NonNull ViewModel viewModel, int action, @NonNull Bundle extra) {
		return onClickListener.onClick(position, viewModel, action, extra);
	}

	@Override
	public void onViewRecycled(@NonNull DifferViewHolder holder) {
		super.onViewRecycled(holder);
		holder.onUnbind();
	}

	public void setMaxRecycledViews(@NonNull RecyclerView recyclerView) {
	}

	@NonNull
	public DividerType getDividerType(@IntRange(from = 0) int position) {
		Class classViewModel = getClassViewModelForLastSpecialDivider();
		if (classViewModel != null) {
			boolean isLastItem = position == getItemCount() - 2;
			if (isLastItem) {
				//noinspection unchecked
				boolean isLastSpecialDivider = getLastItem().getItemViewType() == DifferViewModel.getItemViewType(classViewModel);
				if (isLastSpecialDivider) {
					return DividerType.PADDING_16;
				}
			}
		}

		ViewModel viewModel = getItem(position);
		return viewModel != null ? viewModel.getDividerType() : DividerType.DISABLED;
	}

	/**
	 * Если перед последним элементом списка нужен особый тип Divider,
	 * то нужно переопределить этот метод и вернуть класс ViewModel,
	 * который сигнализирует о классе ViewModel последнего элемента,
	 * перед которым нужен это divider.
	 * <p>
	 * Например, последним элементом списка вакансий идет лоадер дозагрузки страницы с типом LoadingViewModel.
	 * Перед эти лоадером divider должен иметь отступы с двух сторон
	 *
	 * @return Class ViewModel
	 */
	@Nullable
	protected Class<? extends ViewModel> getClassViewModelForLastSpecialDivider() {
		return null;
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
