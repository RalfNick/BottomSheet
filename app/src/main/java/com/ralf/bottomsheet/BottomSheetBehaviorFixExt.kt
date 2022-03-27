package com.ralf.bottomsheet

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.lang.ref.WeakReference

/** BottomSheetBehavior 在多个子 View 情况下滑动冲突处理 */
object BottomSheetBehaviorFixExt {

    @JvmStatic
    internal fun fixBehaviorNestedScroll(containerView: View, behavior: BottomSheetBehavior<*>?) {
        try {
            val field = BottomSheetBehavior::class.java.getDeclaredField("nestedScrollingChildRef")
            field.isAccessible = true
            behavior?.let {
                field.set(it, WeakReference<View>(findScrollingChild(containerView)))
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    private fun findScrollingChild(view: View): View? {
        if (ViewCompat.isNestedScrollingEnabled(view)) {
            return view
        }
        if (view is ViewGroup) {
            val count = view.childCount
            for (i in count - 1 downTo 0) {
                val scrollingChild = findScrollingChild(view.getChildAt(i))
                if (scrollingChild != null) {
                    return scrollingChild
                }
            }
        }
        return null
    }
}