package ru.starksoft.differ.utils

import android.os.Looper

@Suppress("MemberVisibilityCanBePrivate")
object ThreadUtils {

    @JvmStatic
    fun checkMainThread() {
        check(isMainThread) { "Main thread allowed only" }
    }

    val isMainThread: Boolean
        get() = Looper.getMainLooper() == Looper.myLooper()
}
