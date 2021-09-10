package ru.starksoft.differ.utils

import android.os.Looper
import android.util.Log

internal object ThreadUtils {

    private const val TAG = "ThreadUtil"

    @JvmStatic
    fun checkNotMainThread() {
        if (isMainThread()) {
            Log.d(TAG, "${Thread.currentThread().name} ### checkOnMainThread: ")
            throw IllegalStateException("You are not allowed to run this method on main thread")
        }
    }

    @JvmStatic
    fun checkMainThread() {
        if (!isMainThread()) {
            Log.d(TAG, "${Thread.currentThread().name} ### checkOnMainThread: ")
            throw IllegalStateException("Called from wrong thread, expected main, but was called from ${Thread.currentThread().name}")
        }
    }

    @JvmStatic
    fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }
}