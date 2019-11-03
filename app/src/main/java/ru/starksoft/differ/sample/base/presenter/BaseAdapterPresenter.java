package ru.starksoft.differ.sample.base.presenter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import ru.starksoft.differ.Logger;
import ru.starksoft.differ.adapter.DifferLabels;
import ru.starksoft.differ.adapter.OnClickListener;
import ru.starksoft.differ.sample.base.adapter.BaseAdapter;
import ru.starksoft.differ.sample.base.adapter.RefreshLabel;
import ru.starksoft.differ.utils.ExecutorHelper;
import ru.starksoft.differ.viewmodel.ViewModelReused;

public abstract class BaseAdapterPresenter
		implements OnClickListener, BaseAdapter.OnBuildAdapterListener, BaseAdapter.OnRefreshAdapterListener {

	public static final String TAG = "BaseAdapterPresenter";
	// Переменные для вычисления задержки частоты обновления
	private static final int WAITING_TIME = 500;
	private final Logger logger = new Logger() {
		@Override
		public void log(@NonNull Throwable t) {
			Log.e(TAG, "log: t", t);
		}

		@Override
		public void log(@NonNull String tag, @NonNull String message) {
			Log.d(tag, message);
		}

		@Override
		public void log(@NonNull String tag, @NonNull String message, @NonNull Throwable t) {
			Log.e(tag, message, t);
		}

		@Override
		public void w(@NonNull String tag, @NonNull String message) {
			Log.w(tag, message);
		}

		@Override
		public void d(@NonNull String tag, @NonNull String message) {
			Log.d(tag, message);
		}
	};
	private final ViewModelReused viewModelReused = new ViewModelReused(this, logger);
	private final DifferLabels waitingLabels = getViewModelLabels();
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final OnAdapterRefreshedListener onAdapterRefreshedListener;
	private ExecutorHelper executor = ExecutorHelper.newSingleThreadExecutor();
	@Nullable private Runnable refreshListRunnable;
	private long lastTime = 0;
	private volatile boolean isNeedRefresh = false;
	private final Runnable runRefreshAdapter = this::refreshAdapter;

	public BaseAdapterPresenter(@NonNull OnAdapterRefreshedListener onAdapterRefreshedListener) {
		this.onAdapterRefreshedListener = onAdapterRefreshedListener;
		if (executor.isShutdown()) {
			executor = ExecutorHelper.newSingleThreadExecutor();
		}
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

	//    @Override
	//    public synchronized void onShow() {
	//        if (isNeedRefresh) {
	//            isNeedRefresh = false;
	//            refreshAdapter();
	//        }
	//    }

	/**
	 * Запускает асинхронную загрузку данных в методе loadingData()
	 */
	protected final void executeLoadingData() {
		executor.submit(() -> {
			try {
				loadingData();
				onLoadingDataFinished();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Загрузка данных не в UI потоке
	 */
	@WorkerThread
	protected void loadingData() {
	}

	/**
	 * Загрузка данных не в UI потоке завершена
	 */
	@WorkerThread
	protected void onLoadingDataFinished() {
		refreshAdapter();
	}

	/**
	 * Асинхронно обновляет adapter, пересобирая список buildViewModelList()
	 */
	@AnyThread
	@Override
	public final synchronized void refreshAdapter(@Nullable @RefreshLabel int... labels) {
		if (waiting(labels)) {
			logger.d(TAG, "Adapter::" + getClass().getSimpleName() + " WAITING! " + waitingLabels.log());
			return;
		}

		DifferLabels viewModelLabels = getViewModelLabels();
		viewModelLabels.add(waitingLabels.getItems());
		waitingLabels.clear();

		if (!needRefreshing()) {
			return;
		}

		executor.submit(() -> {
			logger.d(TAG, "Adapter::" + getClass().getSimpleName() + " ___ REFRESHING! " + waitingLabels.log());
			long time = System.currentTimeMillis();
			viewModelReused.build();
			logger.d(TAG,
					 "Adapter::" + getClass().getSimpleName() + " buildViewModelList :: " + (System.currentTimeMillis() - time) +
							 " ms !!DONE!! " + viewModelLabels.log());
			//            handler.post(() -> getView().ifPresent(v -> {
			//                if (v instanceof BaseCommonObserver.OnRefreshLoadingListener) {
			//                    ((BaseCommonObserver.OnRefreshLoadingListener) v).onRefreshLoading(false);
			//                }
			//                v.updateAdapter(viewModelReused.getList(), viewModelLabels);
			//            }));

			handler.post(() -> onAdapterRefreshedListener.updateAdapter(viewModelReused.getList(), viewModelLabels));
		});
	}

	@NonNull
	private DifferLabels getViewModelLabels() {
		return DifferLabels.obtain();
	}

	/**
	 * Требуется обновление списка
	 *
	 * @return true - требуется обновление
	 */
	private boolean needRefreshing() {
		if (isNeedRefresh) {
			return false;
		} else if (!isVisible()) {
			logger.d(TAG, "Adapter::" + getClass().getSimpleName() + " ___ HIDDEN! " + waitingLabels.log());
			isNeedRefresh = true;
			return false;
		}

		return true;
	}

	private boolean isVisible() {
		return true;
	}

	/**
	 * Обновление не чаще раза в 500 мс
	 */
	private boolean waiting(@Nullable @RefreshLabel int... labels) {
		long curTime = System.currentTimeMillis();
		long diffTime = curTime - lastTime;

		waitingLabels.add(labels);

		if (lastTime != 0 && diffTime < WAITING_TIME) {
			logger.d(TAG,
					 "Adapter::" + getClass().getSimpleName() + " diffTime " + diffTime + " ms waiting " + (WAITING_TIME - diffTime) +
							 " ms BREAK " + waitingLabels.log());
			handler.removeCallbacksAndMessages(runRefreshAdapter);
			handler.postDelayed(runRefreshAdapter, WAITING_TIME - diffTime);
			logger.d(TAG, "Adapter::" + getClass().getSimpleName() + " buildViewModelList :: END " + waitingLabels.log());
			return true;
		} else {
			logger.d(TAG,
					 "Adapter::" + getClass().getSimpleName() + " diffTime > " + WAITING_TIME + " = " + diffTime + " ms " +
							 waitingLabels.log());
			lastTime = curTime;
			return false;
		}
	}

	//    protected final void setOnLoadingListener(@Nullable BaseCommonObserver.OnLoadingListener onLoadingListener) {
	//        this.onLoadingListener = onLoadingListener;
	//    }

	protected final void setRefreshListener(@NonNull Runnable refreshListRunnable) {
		this.refreshListRunnable = refreshListRunnable;
	}

	@CallSuper
	public void refreshList() {
		if (refreshListRunnable != null) {
			refreshListRunnable.run();
		}
	}

	@UiThread
	public void runAsync(@NonNull Runnable runnable) {
		executor.submit(runnable);
	}

	public void runInUiThread(@NonNull Runnable runnable) {
		handler.post(runnable);
	}
}
