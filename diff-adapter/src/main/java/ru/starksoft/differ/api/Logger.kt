package ru.starksoft.differ.api

interface Logger {

    fun w(tag: String, message: String)

    fun d(tag: String, message: String)

    fun e(tag: String, message: String, t: Throwable)
}
