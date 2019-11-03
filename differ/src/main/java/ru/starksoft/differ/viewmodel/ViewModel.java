package ru.starksoft.differ.viewmodel;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ViewModel {

	/**
	 * Уникальный идентификатор для одного типа модели
	 * </p>
	 * Используется при создании ViewHolder
	 */
	int getItemViewType();

	int getDividerType();

	/**
	 * Уникальный hashcode текущего экземпляра модели в зависимости от ее контента
	 * </p>
	 * Используется для определения (в методе reused()) переиспользовать готовую ViewModel(изменили только ее контент) или нужно создать новую
	 */
	int getContentHashCode();

	/**
	 * Уникальный hashcode текущего экземпляра модели
	 * </p>
	 * Используется для определения (Diff.class) нужно ли использовать анимацию обновления текущего элемента
	 * или анимацию удаления текущего элемента и добавления нового элемента
	 */
	int getItemHashCode();

	@Nullable
	Bundle getChangePayload(@NonNull ViewModel viewModel);

}
