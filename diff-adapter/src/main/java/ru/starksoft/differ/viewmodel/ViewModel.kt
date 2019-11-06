package ru.starksoft.differ.viewmodel

import android.os.Bundle
import ru.starksoft.differ.divider.DividerType

interface ViewModel {

	/**
	 * Уникальный идентификатор для одного типа модели
	 *
	 * Используется при создании ViewHolder
	 */
	fun getItemViewType(): Int

	fun getDividerType(): DividerType

	/**
	 * Уникальный hashcode текущего экземпляра модели в зависимости от ее контента
	 *
	 * Используется для определения (в методе reused()) переиспользовать готовую ViewModel(изменили только ее контент) или нужно создать новую
	 */
	fun getContentHashCode(): Int

	/**
	 * Уникальный hashcode текущего экземпляра модели
	 *
	 * Используется для определения (Diff.class) нужно ли использовать анимацию обновления текущего элемента
	 * или анимацию удаления текущего элемента и добавления нового элемента
	 */
	fun getItemHashCode(): Int

	fun getChangePayload(viewModel: ViewModel): Bundle?

}
