package ru.starksoft.differ.api

import android.util.Log

class LoggerImpl : Logger {

	override fun w(tag: String, message: String) {
		Log.w(tag, message)
	}

	override fun d(tag: String, message: String) {
		Log.d(tag, message)
	}

	override fun e(tag: String, message: String, t: Throwable) {
		Log.e(tag, message, t)
	}

	companion object {
		@JvmStatic
		val instance: Logger = LoggerImpl()
	}
}
