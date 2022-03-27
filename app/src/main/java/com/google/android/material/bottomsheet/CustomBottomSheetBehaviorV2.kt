package com.google.android.material.bottomsheet

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.view.ViewCompat

class CustomBottomSheetBehaviorV2<V : View> : BottomSheetBehavior<V> {

    constructor() : super()
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    @Nullable
    override fun findScrollingChild(view: View): View? {
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

    override fun shouldHide(child: View, yvel: Float): Boolean {
        return child.top.toFloat() + yvel * 0.2f >= fitToContentsOffset + child.height / 2.0f
    }
}