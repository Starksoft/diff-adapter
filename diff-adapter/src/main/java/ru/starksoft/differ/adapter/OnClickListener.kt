package ru.starksoft.differ.adapter

import android.os.Bundle
import androidx.annotation.IntRange
import ru.starksoft.differ.adapter.viewmodel.ViewModel

fun interface OnClickListener {

    fun onClick(@IntRange(from = 0) position: Int, viewModel: ViewModel, action: Int, extra: Bundle): Boolean
}