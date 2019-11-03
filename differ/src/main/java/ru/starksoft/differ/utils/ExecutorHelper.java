package ru.starksoft.differ.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;

public final class ExecutorHelper {

	@NonNull private final ExecutorService executorService;

	private ExecutorHelper(@NonNull ExecutorService executorService) {
		this.executorService = executorService;
	}

	@NonNull
	public static ExecutorHelper newSingleThreadExecutor() {
		return new ExecutorHelper(Executors.newSingleThreadExecutor());
	}

	public void destroy() {
		if (!isShutdown()) {
			executorService.shutdownNow();
		}
	}

	public void submit(@NonNull Runnable runnable) {
		if (!isShutdown()) {
			executorService.submit(runnable);
		}
	}

	public boolean isShutdown() {
		return executorService.isShutdown() || executorService.isTerminated();
	}

}
