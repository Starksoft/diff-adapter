package ru.starksoft.differ.api

import android.os.Handler
import android.os.Looper
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import ru.starksoft.differ.adapter.DifferAdapter
import ru.starksoft.differ.adapter.DifferLabels
import ru.starksoft.differ.adapter.viewmodel.ViewModelReused
import ru.starksoft.differ.utils.ExecutorHelper
import ru.starksoft.differ.utils.ExecutorHelperImpl

abstract class DiffAdapterDataSource(
	private val executorHelper: ExecutorHelper = ExecutorHelperImpl(),
	private val logger: Logger = LoggerImpl.getInstance()
) : DifferAdapter.OnRefreshAdapterListener, DifferAdapter.OnBuildAdapterListener {

	private val viewModelReused = ViewModelReused(this, logger)
	private val waitingLabels = viewModelLabels
	private val handler = Handler(Looper.getMainLooper())
	private var onAdapterRefreshedListener: OnAdapterRefreshedListener? = null
	private var lastTime: Long = 0
	@Volatile
	private var isNeedRefresh = false
	private val runRefreshAdapter = Runnable { refreshAdapter() }

	private val viewModelLabels: DifferLabels
		get() = DifferLabels.obtain()

	private val isVisible: Boolean
		get() = true

	fun setOnAdapterRefreshedListener(onAdapterRefreshedListener: OnAdapterRefreshedListener?) {
		this.onAdapterRefreshedListener = onAdapterRefreshedListener
	}

	/**
	 * Called when Adapter attached to RecyclerView
	 */
	fun onAttach() {
		if (isNeedRefresh) {
			isNeedRefresh = false
			refreshAdapter()
		}
	}

	/**
	 * Called when Adapter detached from RecyclerView
	 */
	fun onDetach() {
		executorHelper.destroy()
		handler.removeCallbacksAndMessages(null)
	}

	//    @CallSuper
	//    @Override
	//    public void attachView(@NonNull V view, @Nullable Bundle savedInstanceState) {
	//        super.attachView(view, savedInstanceState);
	//        if (executor.isShutdown()) {
	//            executor = ExecutorHelper.newSingleThreadExecutor();
	//        }
	//    }
	//
	//    @CallSuper
	//    @Override
	//    public void detachView() {
	//        executor.destroy();
	//        handler.removeCallbacksAndMessages(null);
	//        super.detachView();
	//    }

	/**
	 * Запускает асинхронную загрузку данных в методе loadingData()
	 */
	fun executeLoadingData() {
		executorHelper.submit(Runnable {
			try {
				loadingData()
				onLoadingDataFinished()
			} catch (t: Throwable) {
				logger.e(TAG, "Unhandled exception in executeLoadingData()", t)
				t.printStackTrace()
			}
		})
	}

	/**
	 * Загрузка данных не в UI потоке
	 */
	@WorkerThread
	protected fun loadingData() {
	}

	/**
	 * Загрузка данных не в UI потоке завершена
	 */
	@WorkerThread
	protected fun onLoadingDataFinished() {
		refreshAdapter()
	}

	/**
	 * Асинхронно обновляет adapter, пересобирая список buildViewModelList()
	 */
	@AnyThread
	@Synchronized
	override fun refreshAdapter(vararg labels: Int) {
		if (waiting(*labels)) {
			logger.d(TAG, "Adapter::" + javaClass.simpleName + " WAITING! " + waitingLabels.log())
			return
		}

		val viewModelLabels = viewModelLabels
		viewModelLabels.add(waitingLabels.items)
		waitingLabels.clear()

		if (!needRefreshing()) {
			return
		}

		executorHelper.submit(Runnable {
			logger.d(TAG, "Adapter::" + javaClass.simpleName + " ___ REFRESHING! " + waitingLabels.log())
			val time = System.currentTimeMillis()
			viewModelReused.build()
			val timeTaken = System.currentTimeMillis() - time
			logger.d(
				TAG,
				"Adapter::" + javaClass.simpleName + " buildViewModelList :: " + timeTaken + " ms !!DONE!! " +
						viewModelLabels.log()
			)

			if (timeTaken > 100) {
				logger.w(TAG, "Too much time taken to process buildViewModelList()")
			}

			handler.post {
				onAdapterRefreshedListener?.let {
					it.updateAdapter(viewModelReused.list, viewModelLabels)
				} ?: logger.w(TAG, "onAdapterRefreshedListener == null")
			}
		})
	}

	/**
	 * Требуется обновление списка
	 *
	 * @return true - требуется обновление
	 */
	private fun needRefreshing(): Boolean {
		if (isNeedRefresh) {
			return false
		} else if (!isVisible) {
			logger.d(TAG, "Adapter::" + javaClass.simpleName + " ___ HIDDEN! " + waitingLabels.log())
			isNeedRefresh = true
			return false
		}

		return true
	}

	/**
	 * Обновление не чаще раза в 500 мс
	 */
	private fun waiting(vararg labels: Int): Boolean {
		val curTime = System.currentTimeMillis()
		val diffTime = curTime - lastTime

		waitingLabels.add(*labels)

		if (lastTime != 0L && diffTime < WAITING_TIME) {
			logger.d(
				TAG,
				"Adapter::" + javaClass.simpleName + " diffTime " + diffTime + " ms waiting " + (WAITING_TIME - diffTime) +
						" ms BREAK " + waitingLabels.log()
			)

			handler.removeCallbacksAndMessages(runRefreshAdapter)
			handler.postDelayed(runRefreshAdapter, WAITING_TIME - diffTime)
			logger.d(TAG, "Adapter::" + javaClass.simpleName + " buildViewModelList :: END " + waitingLabels.log())
			return true
		} else {
			logger.d(
				TAG,
				"Adapter::" + javaClass.simpleName + " diffTime > " + WAITING_TIME + " = " + diffTime + " ms " +
						waitingLabels.log()
			)
			lastTime = curTime
			return false
		}
	}

	@UiThread
	fun runAsync(runnable: Runnable) {
		executorHelper.submit(runnable)
	}

	fun runInUiThread(runnable: Runnable) {
		handler.post(runnable)
	}

	companion object {

		private const val TAG = "DiffAdapterDataSource"
		// Переменные для вычисления задержки частоты обновления
		private const val WAITING_TIME = 500
	}
}
