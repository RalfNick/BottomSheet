package com.ralf.bottomsheet.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.CustomBottomSheetBehavior

class BottomSheetRecyclerView : RecyclerView, CustomBottomSheetBehavior.ScrollDownInterceptor {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val mHelper: RecyclerViewPositionHelper by lazy {
        RecyclerViewPositionHelper(this)
    }

    override fun canScrollDown(): Boolean {
        if (childCount <= 0) {
            return false
        }

        val firstChildPosition = mHelper.findFirstVisibleItemPosition()
        if (firstChildPosition > 0) {
            return true
        }
        val child = getChildAt(0)
        val insets = Rect(0, 0, 0, 0)
        getDecoratedBoundsWithMargins(child, insets)
        return insets.top >= 0
    }

}