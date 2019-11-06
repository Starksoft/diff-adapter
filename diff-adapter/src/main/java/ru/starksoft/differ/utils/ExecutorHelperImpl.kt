package ru.starksoft.differ.utils

import java.util.concurrent.Executors

class ExecutorHelperImpl : ExecutorHelper {

	private val executorService = Executors.newSingleThreadExecutor()

	override fun destroy() {
		if (!isShutdown()) {
			executorService.shutdownNow()
		}
	}

	override fun submit(runnable: Runnable) {
		if (!isShutdown()) {
			executorService.submit(runnable)
		}
	}

	override fun isShutdown(): Boolean {
		return executorService.isShutdown || executorService.isTerminated
	}
}
