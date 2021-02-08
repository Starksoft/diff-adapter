package ru.starksoft.differ.adapter.viewmodel

import android.os.Bundle
import ru.starksoft.differ.divider.DividerType

interface ViewModel {

    /**
     * Unique identifier for this model
     *
     * Used when creating the ViewHolder
     */
    fun getItemViewType(): Int

    fun getDividerType(): DividerType

    /**
     * Unique hashcode of the current ViewModel depends on its content
     *
     * Used to determine (in the reused() method) to reuse the ViewModel (only its content has been changed) or we need to create a new one
     */
    fun getContentHashCode(): Int

    /**
     * Unique hashcode of the current model instance
     *
     * Used to determine (Diff.class) whether to use the update animation of the current element
     * or animation of deleting the current item and adding a new item
     */
    fun getItemHashCode(): Int

    fun getChangePayload(viewModel: ViewModel): Bundle?

    /**
     * Sign of the need to scroll to this ViewModel on events: notifyItemRangeInserted, notifyItemMoved, notifyItemRangeChanged
     */
    fun needScrollTo(): Boolean = false

    /**
     * Selects ScrollStrategy for scroll when needScrollTo == true
     */
    fun scrollStrategy(): ScrollStrategy = ScrollStrategy.CENTER

    enum class ScrollStrategy {
        TOP, CENTER
    }
}
