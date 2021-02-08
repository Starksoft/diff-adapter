package ru.starksoft.differ.adapter

import androidx.recyclerview.widget.ListUpdateCallback
import ru.starksoft.differ.adapter.viewmodel.ViewModel

abstract class DifferAdapterEventListener : ListUpdateCallback {

    override fun onChanged(position: Int, count: Int, payload: Any?) {
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
    }

    override fun onInserted(position: Int, count: Int) {
    }

    override fun onRemoved(position: Int, count: Int) {
    }

    abstract fun onFinished(viewModelList: List<ViewModel>)

    abstract fun onBeforeStarted()
}
