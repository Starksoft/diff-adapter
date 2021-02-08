package ru.starksoft.differ.adapter.viewmodel;

import androidx.annotation.NonNull;

/**
 * Интерфейс для получения объекта
 *
 * @param <R>
 */
public interface ContentHashCodeFunc<R> {

    @NonNull
    R call(int contentHashCode);
}
