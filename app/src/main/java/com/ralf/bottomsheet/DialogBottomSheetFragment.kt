package com.ralf.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ralf.bottomsheet.BottomSheetBehaviorFixExt.fixBehaviorNestedScroll
import com.ralf.bottomsheet.utils.ViewUtil

class DialogBottomSheetFragment : BottomSheetDialogFragment(), IBottomSheetOperator {

    companion object {

        private const val TAG = "BottomSheetFragment"
        private const val RADIO_DEFAULT = 0.618f

        fun newInstance(args: Bundle?): DialogBottomSheetFragment {
            return DialogBottomSheetFragment().apply {
                this.arguments = args ?: Bundle()
            }
        }
    }

    private var mRadio = RADIO_DEFAULT
    private var mBottomSheet: FrameLayout? = null
    private var mBehavior: BottomSheetBehavior<FrameLayout>? = null
    var mStateChangedCallback: BottomSheetBehavior.BottomSheetCallback? = null

    override fun getTheme(): Int {
        return if (showsDialog) R.style.MyDialog_Transparent else 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_bottom_sheet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBottomSheetLayout()
//        view.layoutParams?.height =
//            (mRadio * ViewUtil.getScreenHeight(requireActivity())).toInt()
        view.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            view.post {
                if (childFragmentManager.fragments.size > 0) {
                    fixBehaviorNestedScroll(view, getBehavior())
                }
            }
        }
        BottomSheetAnimationUtil.overrideDialogEnterAnimFromBottom(view)
        getBehavior()?.apply {
            peekHeight = (mRadio * ViewUtil.getScreenHeight(requireActivity())).toInt()
//            skipCollapsed = true
            state = BottomSheetBehavior.STATE_COLLAPSED
//            mBottomSheet?.let {
//                mStateChangedCallback?.onStateChanged(
//                    it,
//                    BottomSheetBehavior.STATE_EXPANDED
//                )
//            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            (this as? BottomSheetDialog)?.dismissWithAnimation = true
        }
    }

    override fun closePanel() {
        mBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun initBottomSheetLayout() {
        if (mBottomSheet == null) {
            mBottomSheet = view?.findViewById(R.id.design_bottom_sheet)
        }
        getBehavior()?.apply {
            mBehavior = (dialog as? BottomSheetDialog)?.behavior
            mBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismissInternal()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // LEFT-DO-NOTHING
                }

            })
            mStateChangedCallback?.let {
                mBehavior?.addBottomSheetCallback(it)
            }
        }
    }

    private fun getBehavior() = (dialog as? BottomSheetDialog)?.behavior

    private fun dismissInternal() {
        parentFragmentManager
            .beginTransaction()
            .remove(this)
            .commitAllowingStateLoss()
        if (showsDialog) {
            (activity as? BottomSheetActivity)?.finish()
        }
    }

}