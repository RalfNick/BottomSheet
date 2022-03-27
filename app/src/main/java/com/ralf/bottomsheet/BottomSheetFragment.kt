package com.ralf.bottomsheet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ralf.bottomsheet.utils.ViewUtil

class BottomSheetFragment : Fragment(), IBottomSheetOperator {

    companion object {

        private const val TAG = "BottomSheetFragment"
        private const val RADIO_DEFAULT = 0.618f

        fun newInstance(args: Bundle?): BottomSheetFragment {
            return BottomSheetFragment().apply {
                this.arguments = args ?: Bundle()
            }
        }
    }

    private var mRadio = RADIO_DEFAULT
    private var mBottomSheet: FrameLayout? = null
    private var mBehavior: BottomSheetBehavior<FrameLayout>? = null
    var mStateChangedCallback: BottomSheetBehavior.BottomSheetCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBottomSheetLayout()
        mBottomSheet?.layoutParams?.height =
            (mRadio * ViewUtil.getScreenHeight(requireActivity())).toInt()
        mBottomSheet?.requestLayout()
        BottomSheetAnimationUtil.overrideDialogEnterAnimFromBottom(view)
        mBehavior?.apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
            mBottomSheet?.let {
                mStateChangedCallback?.onStateChanged(
                    it,
                    BottomSheetBehavior.STATE_EXPANDED
                )
            }
        }
    }

    override fun closePanel() {
        mBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initBottomSheetLayout() {
        if (mBottomSheet == null) {
            mBottomSheet = view?.findViewById(R.id.design_bottom_sheet)
        }
        view?.findViewById<View>(R.id.touch_outside)?.apply {
            setOnClickListener {
                closePanel()
            }
        }
        mBottomSheet?.apply {
            setOnTouchListener { _: View?, _: MotionEvent? ->
                // Consume the event and prevent it from falling through
                true
            }
            mBehavior = BottomSheetBehavior.from(this)
            mBehavior?.isHideable = true
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

    private fun dismissInternal() {
        parentFragmentManager
            .beginTransaction()
            .remove(this)
            .commitAllowingStateLoss()
    }

}