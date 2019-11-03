package ru.starksoft.differ.viewmodel;

import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import ru.starksoft.differ.utils.hash.HashCode;

import static ru.starksoft.differ.utils.hash.HashCode.NONE_HASHCODE;

public abstract class DifferViewModel implements ViewModel {

    private final int contentHashCode;
    private int itemHashCode;

    public DifferViewModel(int contentHashCode) {
        this.contentHashCode = contentHashCode;
    }

    public static <M extends ViewModel> int getItemViewType(@NonNull Class<M> clazz) {
        return HashCode.get(clazz);
    }

    /**
     * Уникальный hashcode текущего экземпляра модели в зависимости от ее контента
     * </p>
     * Используется для определения (в методе reused()) переиспользовать готовую ViewModel(изменили только ее контент) или нужно создать новую
     */
    public int getContentHashCode() {
        return contentHashCode;
    }

    @CallSuper
    protected int getItemHashCode(@Nullable Object... list) {
        if (itemHashCode == NONE_HASHCODE) {
            itemHashCode = HashCode.get(getClass(), list);
        }
        return itemHashCode;
    }

    /**
     * Уникальный hashcode текущего экземпляра модели
     * </p>
     * Используется для определения (Diff.class) нужно ли использовать анимацию обновления текущего элемента
     * или анимацию удаления текущего элемента и добавления нового элемента
     */
    @Override
    public int getItemHashCode() {
        if (itemHashCode == NONE_HASHCODE) {
            return contentHashCode;
        } else {
            return itemHashCode;
        }
    }

    /**
     * Уникальный hashcode текущего экземпляра модели
     * </p>
     * Используется для определения (Diff.class) нужно ли использовать анимацию обновления текущего элемента
     * или анимацию удаления текущего элемента и добавления нового элемента
     * </p>
     * В данной реализации все экземпляры ViewModel одного типа имеют одинаковый hashcode
     */
    protected final int getDefaultItemHashCode() {
        return HashCode.get(getClass());
    }

    /**
     * Уникальный идентификатор для одного типа модели
     * </p>
     * Используется при создании ViewHolder
     */
    @Override
    public final int getItemViewType() {
        return getItemViewType(getClass());
    }

    @Nullable
    @Override
    public Bundle getChangePayload(@NonNull ViewModel viewModel) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DifferViewModel that = (DifferViewModel) o;
        return contentHashCode == that.contentHashCode && itemHashCode == that.itemHashCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentHashCode, itemHashCode);
    }

    @NonNull
    @Override
    public String toString() {
        return "DifferViewModel{" + "contentHashCode=" + contentHashCode + ", itemHashCode=" + itemHashCode + '}';
    }
}
