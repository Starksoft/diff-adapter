package ru.starksoft.differ.utils

interface ExecutorHelper {

    fun isShutdown(): Boolean

    fun destroy()

    fun submit(runnable: Runnable)
}
