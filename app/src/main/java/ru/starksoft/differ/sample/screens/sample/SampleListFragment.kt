package ru.starksoft.differ.sample.screens.sample

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.screen_sample_list.*
import ru.starksoft.differ.adapter.OnClickListener
import ru.starksoft.differ.sample.R
import ru.starksoft.differ.sample.base.presenter.OnAdapterRefreshedListener
import ru.starksoft.differ.sample.screens.BaseFragment
import ru.starksoft.differ.sample.screens.sample.adapter.SampleAdapter
import ru.starksoft.differ.sample.screens.sample.adapter.SampleAdapterDelegate

class SampleListFragment : BaseFragment() {

	private val delegate = SampleAdapterDelegate(OnAdapterRefreshedListener { vm, l -> adapter.update(vm, l) })
	private val adapter = SampleAdapter(OnClickListener { position, viewModel, action, extra ->
		false
	})

	override fun getLayoutView(): Int {
		return R.layout.screen_sample_list
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		delegate.populate(1000)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		sampleRecyclerView.layoutManager = LinearLayoutManager(view.context)
		sampleRecyclerView.adapter = adapter

		delegate.refreshAdapter()

	}
}
