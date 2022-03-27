package com.ralf.bottomsheet

import android.os.Bundle
import android.view.View
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.util.*

/** BottomSheet 操作类 */
object BottomSheet {

    private val weakFragmentMap = WeakHashMap<String, Fragment>()

    @JvmStatic
    fun showBottomSheet(option: BottomSheetOption) {
        if (weakFragmentMap[option.bottomSheetTag] == null) {
            val showDialog = option.isShowDialog
            weakFragmentMap[option.bottomSheetTag] =
                if (showDialog) DialogBottomSheetFragment.newInstance(option.args)
                else BottomSheetFragment.newInstance(option.args)
            registerFragmentLifecycleCallbacks(option)
            if (showDialog) {
                showDialogBottomSheet(option)
            } else {
                showNonDialogBottomSheet(option)
            }
        } else {
            weakFragmentMap[option.bottomSheetTag]?.let {
                addChildFragment(it, option.childTag, option.fragment, option.enter, option.exit)
            }
        }
    }

    private fun showDialogBottomSheet(option: BottomSheetOption) {
        (weakFragmentMap[option.bottomSheetTag] as? DialogBottomSheetFragment)?.apply {
            mStateChangedCallback = option.stateChangedCallback
            show(option.fm, option.bottomSheetTag)
        }
    }

    private fun showNonDialogBottomSheet(option: BottomSheetOption) {
        (weakFragmentMap[option.bottomSheetTag] as? BottomSheetFragment)?.apply {
            mStateChangedCallback = option.stateChangedCallback
            option.fm.beginTransaction().let {
                it.add(option.layoutRes, this, option.bottomSheetTag)
                it.commitAllowingStateLoss()
            }
        }
    }

    private fun registerFragmentLifecycleCallbacks(option: BottomSheetOption) {
        option.fm.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(
                fm: FragmentManager,
                f: Fragment,
                v: View,
                savedInstanceState: Bundle?
            ) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState)
                weakFragmentMap[option.bottomSheetTag]?.let {
                    if (f == it) {
                        addChildFragment(
                            it,
                            option.childTag,
                            option.fragment,
                            option.enter,
                            option.exit
                        )
                    }
                }
            }

            override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                super.onFragmentDetached(fm, f)
                weakFragmentMap.remove(option.bottomSheetTag)
                fm.unregisterFragmentLifecycleCallbacks(this)
            }
        }, false)
    }

    @JvmStatic
    private fun addChildFragment(
        bsFragment: Fragment,
        tag: String?,
        fragment: Fragment,
        @AnimatorRes @AnimRes enter: Int = 0,
        @AnimatorRes @AnimRes exit: Int = 0
    ) {
        val childFragmentManager = bsFragment.childFragmentManager
        bsFragment.arguments?.let {
            fragment.arguments?.putAll(it)
        }
        val fragmentTag = tag ?: fragment.javaClass.simpleName
        // 第一个 Fragment 不展示动画，会展示 bottom sheet 动画
        val showAnim = childFragmentManager.fragments.size > 0
        val enterAnim = if (showAnim) enter else 0
        val exitAnim = if (showAnim) exit else 0
        childFragmentManager.findFragmentByTag(fragmentTag)?.let {
            childFragmentManager.beginTransaction()
                .setCustomAnimations(enterAnim, exitAnim)
                .show(it)
                .commitAllowingStateLoss()
        } ?: let {
            childFragmentManager.beginTransaction().run {
                setCustomAnimations(enterAnim, exitAnim)
                add(R.id.design_bottom_sheet, fragment, fragmentTag)
                commitAllowingStateLoss()
            }
        }
    }

    @JvmStatic
    fun removeChildFragment(
        bottomSheetTag: String, childTag: String,
        @AnimatorRes @AnimRes exit: Int = 0
    ) {
        weakFragmentMap[bottomSheetTag]?.childFragmentManager?.apply {
            if (fragments.size > 0) {
                findFragmentByTag(childTag)?.let {
                    beginTransaction()
                        .setCustomAnimations(0, exit)
                        .remove(it)
                        .commitAllowingStateLoss()
                }
            } else {
                hideBottomSheet(bottomSheetTag)
            }
        }
    }

    @JvmStatic
    private fun removeBottomSheet(bottomSheetTag: String) {
        weakFragmentMap[bottomSheetTag]?.let {
            it.parentFragmentManager.apply {
                beginTransaction().remove(it).commitAllowingStateLoss()
            }
        }
    }

    @JvmStatic
    fun hideBottomSheet(bottomSheetTag: String) {
        (weakFragmentMap[bottomSheetTag] as? IBottomSheetOperator)?.closePanel()
    }
}