package ru.starksoft.differ.sample.screens.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.os.ResultReceiver
import android.util.Log
import android.view.View
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.screen_sample_list.*
import ru.starksoft.differ.adapter.DifferAdapterEventListener
import ru.starksoft.differ.adapter.OnClickListener
import ru.starksoft.differ.adapter.viewmodel.ViewModel
import ru.starksoft.differ.api.DiffAdapter
import ru.starksoft.differ.api.LoggerImpl
import ru.starksoft.differ.sample.R
import ru.starksoft.differ.sample.base.BaseFragment
import ru.starksoft.differ.sample.screens.sample.adapter.DiffAdapterDataSourceImpl
import ru.starksoft.differ.sample.screens.sample.adapter.SampleClickAction
import ru.starksoft.differ.sample.screens.sample.adapter.viewholder.DataInfoViewHolder
import ru.starksoft.differ.sample.screens.sample.adapter.viewholder.HeaderViewHolder
import ru.starksoft.differ.sample.screens.sample.adapter.viewholder.SampleViewHolder
import ru.starksoft.differ.sample.screens.sample.adapter.viewmodel.SampleViewModel
import ru.starksoft.differ.sample.screens.sample.dialogs.ActionsBottomSheet
import ru.starksoft.differ.utils.ExecutorHelperImpl

@Keep
@SuppressLint("SetTextI18n")
class SampleListFragment : BaseFragment() {

	private lateinit var diffAdapter: DiffAdapter
	private val adapterDataSource = DiffAdapterDataSourceImpl.create(ExecutorHelperImpl(), LoggerImpl.instance)
	private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
		var orderChanged = false

		override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
			if (source.itemViewType != target.itemViewType) {
				return false
			}

			val from = source.adapterPosition
			val to = target.adapterPosition

			orderChanged = from != to
			recyclerView.adapter?.notifyItemMoved(from, to)

			adapterDataSource.onSortChanged(from, to)
			// Notify the adapter of the move
			//mAdapter.onItemMove(source.getAdapterPosition(), target.adapterPosition)
			return true
		}

		override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
			val sampleViewHolder = viewHolder as? SampleViewHolder
			sampleViewHolder?.let {
				adapterDataSource.remove(sampleViewHolder.viewModel.id)
			}
		}

		override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
			super.onSelectedChanged(viewHolder, actionState)

			if (actionState == ItemTouchHelper.ACTION_STATE_IDLE && orderChanged) {
				orderChanged = false
				adapterDataSource.refreshAdapter(dontTriggerMoves = true)
			}
		}
	})
	private val resultReceiver = object : ResultReceiver(null) {

		@SuppressLint("RestrictedApi")
		override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
			super.onReceiveResult(resultCode, resultData)
			Log.d(TAG, "onReceiveResult() called with: resultCode = [$resultCode], resultData = [$resultData]")

			adapterDataSource.addItems(ActionsBottomSheet.Actions.values()[resultCode], 1)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setHasOptionsMenu(true)
		adapterDataSource.populate(10)

		diffAdapter = DiffAdapter
			.create(adapterDataSource)
			.withViewHolders(SampleViewHolder::class.java, HeaderViewHolder::class.java, DataInfoViewHolder::class.java)
			// Second variant to attach ViewHolders, without reflection
			//			.withFactory(ViewHolderFactory { parent, viewType, onClickListener ->
			//				return@ViewHolderFactory when (viewType) {
			//					DifferViewModel.getItemViewType(SampleViewModel::class.java) -> SampleViewHolder(parent, onClickListener)
			//					DifferViewModel.getItemViewType(HeaderViewModel::class.java) -> HeaderViewHolder(parent, onClickListener)
			//					DifferViewModel.getItemViewType(DataInfoViewModel::class.java) -> DataInfoViewHolder(parent, onClickListener)
			//
			//					else -> throw IllegalStateException("Unknown viewType=$viewType at ${javaClass.simpleName}")
			//				}
			//			})
			.withClickListener(OnClickListener { _, viewModel, action, _ ->
				return@OnClickListener when (action) {
					SampleClickAction.DELETE.ordinal -> {
						adapterDataSource.remove((viewModel as SampleViewModel).id)
						//						activity?.supportFragmentManager?.beginTransaction()?.replace(
						//							R.id.root,
						//							SampleListFragment()
						//						)?.addToBackStack("Sample2")?.commit()
						true
					}
					else -> false
				}
			})
			.withItemTouchHelper(itemTouchHelper)
			.createAdapter()
	}

	override fun onDestroy() {
		super.onDestroy()

		adapterDataSource.addNewItems(2)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		sampleRecyclerView.layoutManager = LinearLayoutManager(view.context)

		sampleActionsButton.setOnClickListener {
			activity?.let { ActionsBottomSheet.show(it.supportFragmentManager, resultReceiver) }
		}

		diffAdapter.attachTo(sampleRecyclerView, createDifferAdapterEventListener(), refreshAdapterOnAttach = true)

		//		executor.scheduleWithFixedDelay({
		//											adapterDataSource.addItems(ActionsBottomSheet.Actions.ADD_TO_END, 3)
		//											adapterDataSource.refreshAdapter()
		//										}, 1, 1, TimeUnit.SECONDS)
	}

	private fun createDifferAdapterEventListener(): DifferAdapterEventListener {

		return object : DifferAdapterEventListener() {
			override fun onFinished(viewModelList: List<ViewModel>) {
				emptyTextView?.isVisible = viewModelList.isEmpty()
			}

			override fun onChanged(position: Int, count: Int, payload: Any?) {
				stateTextView?.text = "changed p=$position, c=$count"
			}

			override fun onMoved(fromPosition: Int, toPosition: Int) {
				stateTextView?.text = "moved fp=$fromPosition, tp=$toPosition"
			}

			override fun onInserted(position: Int, count: Int) {
				stateTextView?.text = "inserted p=$position, c=$count"
			}

			override fun onRemoved(position: Int, count: Int) {
				stateTextView?.text = "removed p=$position, c=$count"
			}
		}
	}

	override fun getLayoutView(): Int {
		return R.layout.screen_sample_list
	}

	companion object {
		private const val TAG = "SampleListFragment"
	}
}
