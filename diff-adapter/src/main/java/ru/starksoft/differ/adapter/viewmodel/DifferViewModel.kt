package ru.starksoft.differ.adapter.viewmodel

import android.os.Bundle
import androidx.annotation.CallSuper
import ru.starksoft.differ.divider.DividerType
import ru.starksoft.differ.utils.hash.HashCode
import ru.starksoft.differ.utils.hash.HashCode.NONE_HASHCODE
import java.util.*
import kotlin.reflect.KClass

abstract class DifferViewModel(private val contentHashCode: Int = NONE_HASHCODE) : ViewModel {

    private var itemHashCode: Int = 0

    /**
     * Уникальный hashcode текущего экземпляра модели
     *
     * Используется для определения (Diff.class) нужно ли использовать анимацию обновления текущего элемента
     * или анимацию удаления текущего элемента и добавления нового элемента
     *
     * В данной реализации все экземпляры ViewModel одного типа имеют одинаковый hashcode
     */
    protected val defaultItemHashCode: Int
        get() = HashCode[javaClass]

    /**
     * Уникальный hashcode текущего экземпляра модели в зависимости от ее контента
     *
     * Используется для определения (в методе reused()) переиспользовать готовую ViewModel(изменили только ее контент) или нужно создать новую
     */
    override fun getContentHashCode(): Int {
        return contentHashCode
    }

    @CallSuper
    protected fun getItemHashCode(vararg list: Any): Int {
        if (itemHashCode == NONE_HASHCODE) {
            itemHashCode = HashCode[javaClass, list]
        }
        return itemHashCode
    }

    /**
     * Уникальный hashcode текущего экземпляра модели
     *
     * Используется для определения (Diff.class) нужно ли использовать анимацию обновления текущего элемента
     * или анимацию удаления текущего элемента и добавления нового элемента
     */
    override fun getItemHashCode(): Int {
        return if (itemHashCode == NONE_HASHCODE) {
            contentHashCode
        } else {
            itemHashCode
        }
    }

    /**
     * Уникальный идентификатор для одного типа модели
     *
     * Используется при создании ViewHolder
     */
    override fun getItemViewType(): Int {
        return getItemViewType(javaClass)
    }

    override fun getChangePayload(viewModel: ViewModel): Bundle? {
        return null
    }

    /**
     * Тип divider, который будет рисовать под этим элементом в списке
     *
     * @return тип divider
     */
    override fun getDividerType(): DividerType {
        return DividerType.DISABLED
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as DifferViewModel?
        return contentHashCode == that?.contentHashCode && itemHashCode == that.itemHashCode
    }

    override fun hashCode(): Int {
        return Objects.hash(contentHashCode, itemHashCode)
    }

    override fun toString(): String {
        return "DifferViewModel{contentHashCode=$contentHashCode, itemHashCode=$itemHashCode}"
    }

    companion object {

        @JvmStatic
        fun <M : ViewModel> getItemViewType(clazz: Class<out M>): Int {
            return HashCode[clazz]
        }
    }
}

fun <M : ViewModel> Class<out M>.getItemViewType(): Int {
    return HashCode[this]
}
