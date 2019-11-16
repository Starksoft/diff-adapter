package ru.starksoft.differ.adapter

import androidx.recyclerview.widget.ListUpdateCallback
import ru.starksoft.differ.adapter.viewmodel.ViewModel

abstract class DifferAdapterEventListener : ListUpdateCallback {

	abstract fun onFinished(viewModelList: List<ViewModel>)
}
