package ru.starksoft.differ.sample.screens.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.screen_sample_list.*
import ru.starksoft.differ.DiffAdapter
import ru.starksoft.differ.adapter.DifferAdapterEventListener
import ru.starksoft.differ.adapter.OnClickListener
import ru.starksoft.differ.adapter.ViewHolderFactory
import ru.starksoft.differ.sample.R
import ru.starksoft.differ.sample.base.BaseFragment
import ru.starksoft.differ.sample.screens.sample.adapter.DiffAdapterDataSourceImpl
import ru.starksoft.differ.sample.screens.sample.adapter.SampleClickAction
import ru.starksoft.differ.sample.screens.sample.adapter.viewholder.SampleViewHolder
import ru.starksoft.differ.sample.screens.sample.adapter.viewmodel.SampleViewModel
import ru.starksoft.differ.utils.ExecutorHelperImpl
import ru.starksoft.differ.viewmodel.DifferViewModel
import ru.starksoft.differ.viewmodel.ViewModel

@SuppressLint("SetTextI18n")
class SampleListFragment : BaseFragment() {

	private val rxAdapterDataSource = DiffAdapterDataSourceImpl.create(ExecutorHelperImpl())

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		rxAdapterDataSource.populate(10)
	}

	override fun onDestroy() {
		super.onDestroy()

		rxAdapterDataSource.addNewItems(2)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		sampleRecyclerView.layoutManager = LinearLayoutManager(view.context)

		sampleAddButton.setOnClickListener {
			rxAdapterDataSource.addNewItems(2)
		}

		DiffAdapter
			.create(rxAdapterDataSource)
			.withFactory(ViewHolderFactory { parent, viewType, onClickListener ->
				return@ViewHolderFactory when (viewType) {
					DifferViewModel.getItemViewType(SampleViewModel::class.java) -> SampleViewHolder(parent, onClickListener)

					else -> throw IllegalStateException("Unknown viewType=$viewType at ${javaClass.simpleName}")
				}
			})
			.withClickListener(OnClickListener { _, viewModel, action, _ ->
				return@OnClickListener when (action) {
					SampleClickAction.DELETE.ordinal -> {
						rxAdapterDataSource.remove((viewModel as SampleViewModel).id)
						true
					}
					SampleClickAction.DELETE_MULTI.ordinal -> {
						(activity as AppCompatActivity?)?.startSupportActionMode(actionModeCallback)
						true
					}
					else -> false
				}
			})
			.attachTo(sampleRecyclerView, createDifferAdapterEventListener())
	}

	private fun createDifferAdapterEventListener(): DifferAdapterEventListener {

		return object : DifferAdapterEventListener {
			override fun onFinished(viewModelList: List<ViewModel>) {
				val visibility = if (viewModelList.isEmpty()) {
					VISIBLE
				} else {
					GONE
				}
				emptyTextView?.visibility = visibility
			}

			override fun onChanged(position: Int, count: Int, payload: Any?) {
				stateTextView?.text = "changed p=$position, c=$count"
			}

			override fun onMoved(fromPosition: Int, toPosition: Int) {
				stateTextView?.text = "moved fp=$fromPosition, tp=$toPosition"
			}

			override fun onInserted(position: Int, count: Int) {
				stateTextView?.text = "inserted p=$position, c=$count"
				sampleRecyclerView?.smoothScrollToPosition(position)
			}

			override fun onRemoved(position: Int, count: Int) {
				stateTextView?.text = "removed p=$position, c=$count"
			}
		}
	}

	private val actionModeCallback = object : ActionMode.Callback {

		// Called when the action mode is created; startActionMode() was called
		override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
			// Inflate a menu resource providing context menu items
			val inflater = mode.menuInflater
			inflater.inflate(R.menu.context_menu, menu)
			return true
		}

		// Called each time the action mode is shown. Always called after onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
			return false // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
			return when (item.itemId) {
				R.id.delete -> {
					mode.finish() // Action picked, so close the CAB
					true
				}
				else -> false
			}
		}

		// Called when the user exits the action mode
		override fun onDestroyActionMode(mode: ActionMode) {
			//			actionMode = null
		}
	}

	override fun getLayoutView(): Int {
		return R.layout.screen_sample_list
	}

	companion object {
		private const val TAG = "SampleListFragment"
	}
}
