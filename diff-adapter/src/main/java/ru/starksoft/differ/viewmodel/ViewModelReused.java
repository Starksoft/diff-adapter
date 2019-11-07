package ru.starksoft.differ.viewmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.collection.SparseArrayCompat;
import ru.starksoft.differ.Logger;
import ru.starksoft.differ.adapter.DifferAdapter;
import ru.starksoft.differ.utils.hash.HashCode;

import static ru.starksoft.differ.utils.hash.HashCode.NONE_HASHCODE;

/**
 * Хранилище ViewModel'ей для повторного использования
 */
public final class ViewModelReused {

	private static final String TAG = "ViewModelReused";

	private final List<ViewModel> viewModelList = new ArrayList<>();
	private final SparseArrayCompat<ViewModel> reusedViewModelList = new SparseArrayCompat<>();
	@NonNull private final Logger logger;
	@NonNull private final DifferAdapter.OnBuildAdapterListener onBuildAdapterListener;

	public ViewModelReused(@NonNull DifferAdapter.OnBuildAdapterListener onBuildAdapterListener, @NonNull Logger logger) {
		this.onBuildAdapterListener = onBuildAdapterListener;
		this.logger = logger;
	}

	/**
	 * Добавляет ViewModel
	 *
	 * @param viewModel ViewModel
	 */
	@WorkerThread
	public void add(@NonNull ViewModel viewModel) {
		synchronized (ViewModelReused.class) {
			viewModelList.add(viewModel);
		}
	}

	/**
	 * Добавляет ViewModel
	 *
	 * @param position  индекс куда добавляется ViewModel
	 * @param viewModel ViewModel
	 */
	@WorkerThread
	public void add(@IntRange(from = 0) int position, @NonNull ViewModel viewModel) {
		synchronized (ViewModelReused.class) {
			viewModelList.add(position, viewModel);
		}
	}

	/**
	 * Добавляет ViewModel
	 *
	 * @param list список ViewModel'ей
	 */
	@WorkerThread
	public void addAll(@NonNull Collection<? extends ViewModel> list) {
		viewModelList.addAll(list);
	}

	/**
	 * Добавляет ViewModel если ее нет в списке reusedViewModelList
	 *
	 * @param position   индекс куда добавляется ViewModel
	 * @param clazz      им класса ViewModel
	 * @param func       лямбда считающая contentHashCode текущей ViewModel
	 * @param objectList список объектов для вычисления contentHashCode
	 */
	@WorkerThread
	public <T extends ViewModel> void add(@IntRange(from = 0) int position, @NonNull Class<T> clazz,
										  @NonNull ContentHashCodeFunc<ViewModel> func, @NonNull Object... objectList) {
		int hashCode = HashCode.get(clazz, objectList);
		ViewModel viewModel = reusedViewModelList.get(hashCode);
		add(position, viewModel == null ? func.call(hashCode) : viewModel);
	}

	/**
	 * Добавляет ViewModel если ее нет в списке reusedViewModelList
	 *
	 * @param clazz      им класса ViewModel
	 * @param func       лямбда, считающая contentHashCode текущей ViewModel
	 * @param objectList список объектов для вычисления contentHashCode
	 */
	@WorkerThread
	public <T extends ViewModel> void add(@NonNull Class<T> clazz, @NonNull ContentHashCodeFunc<ViewModel> func,
										  @NonNull Object... objectList) {
		int hashCode = HashCode.get(clazz, objectList);
		ViewModel viewModel = reusedViewModelList.get(hashCode);
		add(viewModel == null ? func.call(hashCode) : viewModel);
	}

	/**
	 * Последняя ViewModel
	 *
	 * @return ViewModel
	 */
	@WorkerThread
	public ViewModel getLast() {
		return viewModelList.get(size() - 1);
	}

	/**
	 * Количество ViewModel'ей
	 *
	 * @return int
	 */
	@WorkerThread
	public int size() {
		return viewModelList.size();
	}

	/**
	 * Список ViewModel'ей
	 *
	 * @return List<ViewModel>
	 */
	@WorkerThread
	public List<ViewModel> getList() {
		synchronized (ViewModelReused.class) {
			return new ArrayList<>(viewModelList);
		}
	}

	/**
	 * Асинхронно обновляет adapter, пересобирая список OnBuildAdapterListener.buildViewModelList()
	 */
	@WorkerThread
	public void build() {
		synchronized (ViewModelReused.class) {
			viewModelList.clear();
			try {
				onBuildAdapterListener.buildViewModelList(this);
			} catch (Throwable t) {
				logger.log(TAG, "buildViewModelList failed ", t);
			}
			reused();
		}
	}

	/**
	 * Переиспользует ViewModel'и
	 * <p>
	 * Если contentHashCode равен NONE_HASHCODE, то такая ViewModel не переиспользуется!
	 */
	@WorkerThread
	private void reused() {
		reusedViewModelList.clear();
		for (ViewModel viewModel : viewModelList) {
			if (viewModel != null) {
				int hash = viewModel.getContentHashCode();
				if (hash != NONE_HASHCODE) {
					reusedViewModelList.put(hash, viewModel);
				}
			}
		}
	}

	/**
	 * Интерфейс для получения объекта
	 *
	 * @param <R>
	 */
	public interface ContentHashCodeFunc <R> {
		@NonNull
		R call(int contentHashCode);
	}
}
