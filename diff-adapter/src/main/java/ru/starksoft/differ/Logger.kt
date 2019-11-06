package ru.starksoft.differ

interface Logger {

	fun log(t: Throwable)

	fun log(tag: String, message: String)

	fun log(tag: String, message: String, t: Throwable)

	fun w(tag: String, message: String)

	fun d(tag: String, message: String)

	fun e(tag: String, message: String, t: Throwable)
}
