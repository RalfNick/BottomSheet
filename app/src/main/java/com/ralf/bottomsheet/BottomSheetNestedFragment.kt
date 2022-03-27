package com.ralf.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.ralf.bottomsheet.demo.NestedPageFragment1
import com.ralf.bottomsheet.widget.NestedFragmentHelper

class BottomSheetNestedFragment : Fragment() {

    companion object {

        private const val TAG = "NestedFragment"

        fun showPanel(
            fm: FragmentManager,
            layoutId: Int,
            args: Bundle? = null
        ): BottomSheetNestedFragment {
            return BottomSheetNestedFragment().apply {
                args?.let {
                    this.arguments = it
                }
                fm.beginTransaction()
                    .replace(layoutId, this, TAG)
                    .commitAllowingStateLoss()
            }
        }
    }

    private var mAlpha = 0.2f
    private val mHelper = NestedFragmentHelper()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_nested_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.argb((255 * mAlpha).toInt(), 0, 0, 0))
        addChildFragment("NestedPageFragment1", NestedPageFragment1.newInstance("", ""))
        mHelper.bind(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHelper.unbind()
    }

    fun closePanel() {
        mHelper.startHideAnimation()
    }

    fun addChildFragment(tag: String, fragment: Fragment) {
        childFragmentManager.findFragmentByTag(tag)?.let {
            childFragmentManager.beginTransaction()
                .show(it)
                .commitAllowingStateLoss()
        } ?: let {
            childFragmentManager.beginTransaction().run {
                add(R.id.design_bottom_sheet, fragment, tag)
                commitAllowingStateLoss()
            }
        }
    }

    fun removeChildFragment(tag: String) {
        childFragmentManager.run {
            findFragmentByTag(tag)?.let {
                this.beginTransaction().remove(it).commitAllowingStateLoss()
            }
        }
    }
}