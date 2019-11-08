package ru.starksoft.differ.adapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.util.Pools;
import ru.starksoft.differ.adapter.viewmodel.ViewModel;

/**
 * Связка списка ViewModel'ей и меток обновления
 * <p>
 * Метки передаеются в refreshAdapter(метка) и обрабатываюся в completedUpdateAdapter()
 */
class DifferRefreshingData {

	private static final int MAX_SIZE_POOL = 20;
	private static final Pools.Pool<DifferRefreshingData> refreshingDataPool = new Pools.SynchronizedPool<>(MAX_SIZE_POOL);
	@NonNull private final List<ViewModel> items = new ArrayList<>();
	@NonNull private DifferLabels labels;

	private DifferRefreshingData() {
		labels = new DifferLabels();
	}

	@NonNull
	public static DifferRefreshingData obtain() {
		DifferRefreshingData differRefreshingData = refreshingDataPool.acquire();
		if (differRefreshingData == null) {
			differRefreshingData = new DifferRefreshingData();
		}
		return differRefreshingData;
	}

	void release() {
		items.clear();
		labels.release();
		refreshingDataPool.release(this);
	}

	void init(@NonNull List<ViewModel> items, @NonNull DifferLabels labels) {
		this.items.clear();
		this.items.addAll(items);

		this.labels = new DifferLabels();
		this.labels.add(labels.getItems());
	}

	@NonNull
	List<ViewModel> getItems() {
		return items;
	}

	@NonNull
	DifferLabels getLabels() {
		return labels;
	}
}
