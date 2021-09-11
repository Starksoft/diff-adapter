package ru.starksoft.differ.adapter.viewmodel

/**
 * Интерфейс для получения объекта
 *
 * @param <R>
</R> */
fun interface ContentHashCodeFunc<R> {

    fun call(contentHashCode: Int): R
}