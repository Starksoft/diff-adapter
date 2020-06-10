package ru.starksoft.differ.adapter.viewmodel

import androidx.annotation.AnyThread
import androidx.annotation.IntRange
import androidx.annotation.WorkerThread
import androidx.collection.SparseArrayCompat
import ru.starksoft.differ.adapter.DifferAdapter
import ru.starksoft.differ.api.Logger
import ru.starksoft.differ.utils.hash.HashCode
import ru.starksoft.differ.utils.hash.HashCode.NONE_HASHCODE
import java.util.*

/**
 * Хранилище ViewModel'ей для повторного использования
 */
class ViewModelReused(private val onBuildAdapterListener: DifferAdapter.OnBuildAdapterListener, private val logger: Logger) {

	private val viewModelList = ArrayList<ViewModel>()
	private val reusedViewModelList = SparseArrayCompat<ViewModel>()

	/**
	 * Последняя ViewModel
	 *
	 * @return ViewModel
	 */
	val last: ViewModel
		@WorkerThread
		get() = viewModelList[size() - 1]

	/**
	 * Список ViewModel'ей
	 *
	 * @return List<ViewModel>
	</ViewModel> */
	val list: MutableList<ViewModel>
		@AnyThread
		get() = synchronized(ViewModelReused::class.java) {
			return ArrayList(viewModelList)
		}

	/**
	 * Добавляет ViewModel
	 *
	 * @param viewModel ViewModel
	 */
	@WorkerThread
	fun add(viewModel: ViewModel) {
		synchronized(ViewModelReused::class.java) {
			viewModelList.add(viewModel)
		}
	}

	/**
	 * Добавляет ViewModel
	 *
	 * @param position  индекс куда добавляется ViewModel
	 * @param viewModel ViewModel
	 */
	@WorkerThread
	fun add(@IntRange(from = 0) position: Int, viewModel: ViewModel) {
		synchronized(ViewModelReused::class.java) {
			viewModelList.add(position, viewModel)
		}
	}

	/**
	 * Добавляет ViewModel
	 *
	 * @param list список ViewModel'ей
	 */
	@WorkerThread
	fun addAll(list: Collection<ViewModel>) {
		viewModelList.addAll(list)
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
	fun <T : ViewModel> add(
		@IntRange(from = 0) position: Int, clazz: Class<T>,
		func: ContentHashCodeFunc<ViewModel>, vararg objectList: Any
	) {
		val hashCode = HashCode[clazz, objectList]
		val viewModel = reusedViewModelList.get(hashCode)
		add(position, viewModel ?: func.call(hashCode))
	}

	/**
	 * Добавляет ViewModel если ее нет в списке reusedViewModelList
	 *
	 * @param clazz      имя класса ViewModel
	 * @param func       лямбда, считающая contentHashCode текущей ViewModel
	 * @param objectList список объектов для вычисления contentHashCode
	 */
	@WorkerThread
	fun <T : ViewModel> add(
		clazz: Class<T>, func: ContentHashCodeFunc<ViewModel>,
		vararg objectList: Any
	) {
		val hashCode = HashCode[clazz, objectList]
		val viewModel = reusedViewModelList.get(hashCode)
		add(viewModel ?: func.call(hashCode))
	}

	/**
	 * Количество ViewModel'ей
	 *
	 * @return int
	 */
	@WorkerThread
	fun size(): Int {
		return viewModelList.size
	}

	/**
	 * Асинхронно обновляет adapter, пересобирая список OnBuildAdapterListener.buildViewModelList()
	 */
	@WorkerThread
	fun build() {
		synchronized(ViewModelReused::class.java) {
			viewModelList.clear()
			try {
				onBuildAdapterListener.buildViewModelList(this)
			} catch (t: Throwable) {
				logger.e(TAG, "buildViewModelList failed ", t)
			}

			reused()
		}
	}

	/**
	 * Переиспользует ViewModel'и
	 *
	 *
	 * Если contentHashCode равен NONE_HASHCODE, то такая ViewModel не переиспользуется!
	 */
	@WorkerThread
	private fun reused() {
		reusedViewModelList.clear()
		for (viewModel in viewModelList) {
			val hash = viewModel.getContentHashCode()
			if (hash != NONE_HASHCODE) {
				reusedViewModelList.put(hash, viewModel)
			}
		}
	}

	companion object {

		private const val TAG = "ViewModelReused"
	}
}

/**
 * @objectList - Список объектов по которым будет высчитываться хэш объекта,
 * для определения необходимости создания нового экземпляра ViewModel
 * @lazyCreateViewModel - Функция вызывается если ViewModel не найден в пуле и
 * нужно создать новый объект
 */
inline fun <reified T : ViewModel> ViewModelReused.addEx(vararg objectList: Any, crossinline lazyCreateViewModel: (Int) -> T) {
	add(T::class.java, ContentHashCodeFunc<ViewModel> { lazyCreateViewModel(it) }, objectList)
}
