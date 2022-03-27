package com.google.android.material.bottomsheet

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import kotlin.math.abs

class CustomBottomSheetBehavior<V : View> : BottomSheetBehavior<V> {

    companion object {
        private const val TAG = "CustomBehavior"
    }

    interface ScrollDownInterceptor {
        fun canScrollDown(): Boolean
    }

    constructor() : super()
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var mEnableIntercept = true
    private var mTouchSlop = 0
    private var mLastX = 0
    private var mLaseY = 0
    private val mChildLocationArray = IntArray(2)

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: V,
        event: MotionEvent
    ): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            mLastX = event.x.toInt()
            mLaseY = event.y.toInt()
        } else if (event.actionMasked == MotionEvent.ACTION_MOVE) {
            if (mTouchSlop == 0) {
                mTouchSlop = ViewConfiguration.get(parent.context).scaledTouchSlop
            }
            val dx = abs(mLastX - event.x)
            val dy = abs(mLaseY - event.y)
            if (mEnableIntercept
                && (mLaseY - event.y) < 0
                && dx < dy
                && dy > mTouchSlop
                && !hasScrollDownChild(parent, event)
            ) {
                return true
            }
        }
        return super.onInterceptTouchEvent(parent, child, event)
    }

    private fun hasScrollDownChild(view: View, event: MotionEvent): Boolean {
        view.getLocationOnScreen(mChildLocationArray)
        val rawX = event.rawX
        val rawY = event.rawY
        if (rawX < mChildLocationArray[0]
            || rawX > mChildLocationArray[0] + view.width
            || rawY < mChildLocationArray[1]
            || rawY > mChildLocationArray[1] + view.height
        ) {
            return false
        }

        if (view is ScrollDownInterceptor) {
            if (view.canScrollDown()) {
                return true
            }
        }
        if (view is ViewGroup) {
            (0 until view.childCount).forEach { index ->
                if (hasScrollDownChild(view.getChildAt(index), event)) {
                    return true
                }
            }
        }
        return false
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        try {
            return super.onTouchEvent(parent, child, event)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "CustomBottomSheetBehavior onTouchEvent:", e)
        }
        return false
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout, child: V,
        directTargetChild: View, target: View, axes: Int, type: Int
    ): Boolean {
        if (axes and ViewCompat.SCROLL_AXIS_VERTICAL == 0) {
            touchingScrollingChild = false
        }
        return super.onStartNestedScroll(
            coordinatorLayout,
            child,
            directTargetChild,
            target,
            axes,
            type
        )
    }

    override fun shouldHide(child: View, yvel: Float): Boolean {
        return child.top.toFloat() + yvel * 0.2f >= fitToContentsOffset + child.height / 2.0f
    }
}