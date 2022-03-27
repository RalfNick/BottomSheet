package com.ralf.bottomsheet.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import com.ralf.bottomsheet.utils.ViewUtil

class BottomSheetNestedLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), NestedScrollingParent3 {

    companion object {
        const val TAG = "BottomSheetNestedLayout"
    }

    var mOnTopChanged: ((top: Int) -> Unit)? = null
    var mOnDragOutEvent: (() -> Unit)? = null
    private val mMaxDragSlop: Int = ViewUtil.dip2px(getContext(), 30f)
    private val mParentHelper: NestedScrollingParentHelper by lazy {
        NestedScrollingParentHelper(this)
    }
    private val mTouchSlop by lazy {
        ViewConfiguration.get(context).scaledTouchSlop
    }
    private var mInitPosition = 0f
    private var mLastY: Int = 0

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return isEnabled
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type)
        onTopChanged()
    }

    override fun onNestedFling(
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        val nestedFling = !isUnderLollipop()
                && super.onNestedFling(target, velocityX, velocityY, consumed)
        onTopChanged()
        return nestedFling
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        val nestedPreFling = !isUnderLollipop()
                && super.onNestedPreFling(target, velocityX, velocityY)
        onTopChanged()
        return nestedPreFling
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        if (!isEnabled) {
            return
        }
        mParentHelper.onStopNestedScroll(target, type)
        onStopScroll()
    }

    private fun onStopScroll() {
        if (getOffsetFromInitPosition() > height / 2) {
            // 关闭
            val anim =
                ValueAnimator.ofFloat(getOffsetFromInitPosition().toFloat(), height.toFloat())
            anim.duration = 150
            anim.addUpdateListener { animation: ValueAnimator ->
                val top = (animation.animatedValue as Float).toInt()
                setOffsetFromInitPosition(top)
                onTopChanged(top)
            }
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    isEnabled = true
                    mOnDragOutEvent?.invoke()
                }

                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    isEnabled = false
                }
            })
            anim.start()
        } else if (getOffsetFromInitPosition() != 0 && getOffsetFromInitPosition() < height / 2) {
            // 回弹
            val anim = ValueAnimator.ofFloat(getOffsetFromInitPosition().toFloat(), 0f)
            anim.duration = 150
            anim.addUpdateListener { animation: ValueAnimator ->
                val top = (animation.animatedValue as Float).toInt()
                setOffsetFromInitPosition(top)
                onTopChanged(top)
            }
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    isEnabled = true
                }

                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    isEnabled = false
                }
            })
            anim.start()
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        this.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        if (!isUnderLollipop()) {
            super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        }
        onTopChanged()
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (!isEnabled) {
            return
        }
        if (!target.canScrollHorizontally(dx)) {
            consumed[0] += dx
        }
        if (dy == 0) {
            return
        }
        // 向下滑动
        if (dy < 0) {
            // 在顶部
            if (!target.canScrollVertically(-1)) {
                scrollByOffset(-dy)
                consumed[1] += dy
            }
        }
        // 向上滑动
        else {
            // 未达到顶部
            if (getOffsetFromInitPosition() - dy > 0) {
                scrollByOffset(-dy.toFloat())
                consumed[1] += dy
            }
            // 到达顶部
            else if (getOffsetFromInitPosition() != 0 && getOffsetFromInitPosition() - dy < 0) {
                val consumedY = dy - getOffsetFromInitPosition()
                scrollByOffset(-getOffsetFromInitPosition())
                consumed[1] += consumedY
            }
        }
        onTopChanged()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return super.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mLastY= (event.y + 0.5f).toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val cy = (event.y + 0.5f).toInt()
                var dy: Int = mLastY - cy
                if (dy > 0) {
                    dy = Math.max(0, dy - mTouchSlop)
                } else {
                    dy = Math.min(0, dy + mTouchSlop)
                }
                Log.d(TAG, "onTouchEvent: dy "  + dy)
                handleSelfVerticalScroll(dy.toInt())
                onTopChanged()
                mLastY = cy
            }
            MotionEvent.ACTION_UP -> {
                onStopScroll()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleSelfVerticalScroll(dy: Int) {
        if (dy == 0) {
            return
        }
        // 向下滑动
        if (dy < 0) {
            Log.d(TAG, "handleSelfVerticalScroll: 11 dy "  +dy)
            scrollByOffset(-dy)
        }
        // 向上滑动
        else {
            // 未达到顶部
            if (getOffsetFromInitPosition() - dy > 0) {
                scrollByOffset(-dy.toFloat())
            }
            // 到达顶部
            else if (getOffsetFromInitPosition() != 0 && getOffsetFromInitPosition() - dy < 0) {
                scrollByOffset(-getOffsetFromInitPosition())
            }
        }
    }

    private fun scrollByOffset(offset: Float) {
        translationY += offset
    }

    private fun isUnderLollipop(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
    }

    fun getInitPosition(): Float {
        return mInitPosition
    }

    fun setInitPosition(initPosition: Float) {
        mInitPosition = initPosition
        translationY = mInitPosition
    }

    /**
     * 正数往下，负数往上
     */
    private fun getOffsetFromInitPosition(): Int {
        return (translationY - mInitPosition).toInt()
    }

    /**
     * 正数往下，负数往上
     */
    fun setOffsetFromInitPosition(f: Int) {
        translationY = f + mInitPosition
    }

    private fun scrollByOffset(offset: Int) {
        translationY += offset
    }

    private fun onTopChanged(top: Int = getOffsetFromInitPosition()) {
        mOnTopChanged?.invoke(top)
    }

    private fun onOnDragOut() {
        mOnDragOutEvent?.invoke()
    }
}