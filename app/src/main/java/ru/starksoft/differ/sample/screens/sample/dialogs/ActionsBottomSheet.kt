package ru.starksoft.differ.sample.screens.sample.dialogs

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.os.ResultReceiver
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_actions.*
import ru.starksoft.differ.sample.R

class ActionsBottomSheet : BottomSheetDialogFragment() {

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.dialog_actions, container, false)
	}

	@SuppressLint("RestrictedApi")
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val resultReceiver: ResultReceiver = arguments!!["resultReceiver"] as ResultReceiver

		actionsAddTwoStart.setOnClickListener {
			resultReceiver.send(Actions.ADD_TO_START.ordinal, null)
			dismissDelayed()
		}
		actionsAddTwoEnd.setOnClickListener {
			resultReceiver.send(Actions.ADD_TO_END.ordinal, null)
			dismissDelayed()
		}
		actionsAddTwoCenter.setOnClickListener {
			resultReceiver.send(Actions.ADD_TO_CENTER.ordinal, null)
			dismissDelayed()
		}
		actionsSwap0to1.setOnClickListener {
			resultReceiver.send(Actions.SWAP_0_TO_1.ordinal, null)
			dismissDelayed()
		}
	}

	private fun dismissDelayed() {
		view?.postDelayed({ dismiss() }, 200)
	}

	override fun onCancel(dialog: DialogInterface) {
		super.onCancel(dialog)
		Log.d(TAG, "onCancel() called with: dialog = [$dialog]")
	}

	override fun onDismiss(dialog: DialogInterface) {
		super.onDismiss(dialog)
		Log.d(TAG, "onDismiss() called with: dialog = [$dialog]")
	}

	enum class Actions {
		ADD_TO_START, ADD_TO_CENTER, ADD_TO_END, SWAP_0_TO_1
	}

	companion object {

		private const val TAG = "ActionsBottomSheet"

		fun show(fragmentManager: FragmentManager, resultReceiver: ResultReceiver) {
			ActionsBottomSheet().apply {
				arguments = Bundle().apply { putParcelable("resultReceiver", resultReceiver) }
			}.show(fragmentManager, TAG)
		}
	}
}
