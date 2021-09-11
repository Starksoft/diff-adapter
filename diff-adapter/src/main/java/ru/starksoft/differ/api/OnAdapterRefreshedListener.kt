package ru.starksoft.differ.api

import androidx.annotation.UiThread
import ru.starksoft.differ.adapter.DifferLabels
import ru.starksoft.differ.adapter.viewmodel.ViewModel

fun interface OnAdapterRefreshedListener {

    @UiThread
    fun updateAdapter(viewModels: List<ViewModel>, labels: DifferLabels, dontTriggerMoves: Boolean)
}