package com.ralf.bottomsheet.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.View
import com.ralf.bottomsheet.BottomSheetNestedFragment
import com.ralf.bottomsheet.R
import com.ralf.bottomsheet.utils.ViewUtil

class NestedFragmentHelper {

    private companion object {
        private const val TAG = "NestedViewAnimHelper"
        private const val SHOW_DIALOG_ANIM_DURATION_NEW = 400
        private const val HIDE_DIALOG_CUBIC_ANIM_DURATION = 400
        private const val ANIMATION_STEP_SHOW_START = 1
        private const val ANIMATION_STEP_SHOW_END = 2
        private const val ANIMATION_STEP_HIDE_START = 3
        private const val ANIMATION_STEP_HIDE_END = 4

        fun getRealtimeCommentPanelHeight(activity: Activity?): Int {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (activity != null && activity.isInMultiWindowMode) {
                    return (ViewUtil.getDisplayHeight(activity) * 0.618).toInt()
                }
            }
            var height = (ViewUtil.getScreenHeight(activity) * 0.618).toInt()
            var displayHeight = 0
            if (activity != null) {
                //部分机型获取height比较不准，这样做一个兜底。如果获取的屏幕高度 小于decorView高度的一半就取decor的高度一半
                displayHeight = (ViewUtil.getDisplayHeight(activity) * 0.5).toInt()
            }
            if (displayHeight > height) {
                height = displayHeight
            }
            return height
        }
    }

    private val mListener =
        View.OnLayoutChangeListener { _: View?, _: Int, top: Int, _: Int, bottom: Int, _: Int, _: Int, _: Int, _: Int ->
            if (bottom - top > 0) {
                updateShowedHeight(TabPanelChangeFrom.TYPE_UNKNOWN)
            }
        }
    private var mHideAnimFromDrag: Boolean = false
    private var mHeight: Int = 0
    private var mActivity: Activity? = null
    private var mContent: BottomSheetNestedLayout? = null
    private var mAnimation: ValueAnimator? = null
    private var mAnimationStarter: AnimationStarter? = null
    private var mFragment: BottomSheetNestedFragment? = null

    fun bind(fragment: BottomSheetNestedFragment) {
        mFragment = fragment
        mActivity = fragment.activity
        mHeight = getRealtimeCommentPanelHeight(mActivity)
        initContentView(fragment)
        fragment.view?.findViewById<View>(R.id.touch_outside)?.setOnClickListener {
            startHideAnimation()
        }
        startShowAnimation()
    }

    private fun initContentView(fragment: BottomSheetNestedFragment) {
        mContent = fragment.view?.findViewById(R.id.design_bottom_sheet)
        mContent?.apply {
            mOnDragOutEvent = { dismissInternal() }
            mOnTopChanged = {
                updateShowedHeight(TabPanelChangeFrom.TYPE_HIDE_ANIMATION_BY_DRAG)
                mHideAnimFromDrag = it > 0
            }
            addOnLayoutChangeListener(mListener)
            setOffsetFromInitPosition(mHeight)
            val h = layoutParams?.height ?: 0
            if (h != mHeight) {
                layoutParams?.height = mHeight
                requestLayout()
            }
        }
    }

    fun startShowAnimation() {
        if (mActivity == null || mContent == null) {
            return
        }
        if (mAnimation != null && mAnimation?.isStarted == true || mAnimationStarter != null) {
            return
        }
        mContent?.let {
            if (it.layoutParams?.height != mHeight) {
                it.layoutParams.height = mHeight
                it.setOffsetFromInitPosition(mHeight)
                it.requestLayout()
                it.post { startShowAnimation() }
                return
            }
        }
        mContent?.let {
            mAnimation = ObjectAnimator.ofFloat(
                it, "translationY",
                it.translationY, it.getInitPosition()
            )
        }
        mAnimation?.apply {
            duration = SHOW_DIALOG_ANIM_DURATION_NEW.toLong()
            interpolator = NestedFragmentAnimExt.createInterpolator()
            addUpdateListener { updateShowedHeight(TabPanelChangeFrom.TYPE_SHOW_ANIMATION) }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    mAnimationStarter = null
                    mContent?.isEnabled = false
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    mAnimation = null
                    mContent?.isEnabled = true
                    mAnimationStarter = null
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    updateShowedHeight(TabPanelChangeFrom.TYPE_SHOW_ANIMATION)
                    mAnimation = null
                    mContent?.isEnabled = true

                }
            })
        }
        mContent?.let {
            mAnimation?.let { anim ->
                mAnimationStarter = AnimationStarter(it, anim)
            }
        }
        mAnimationStarter?.start()
    }


    fun startHideAnimation() {
        if (mActivity == null || mContent == null) {
            return
        }
        if (mAnimation != null && mAnimation?.isStarted == true || mAnimationStarter != null) {
            return
        }
        mContent?.let {
            mAnimation = ObjectAnimator.ofFloat(
                it, "translationY",
                mContent!!.translationY,
                mContent!!.translationY + mHeight
            )
        }
        mAnimation?.apply {
            duration = HIDE_DIALOG_CUBIC_ANIM_DURATION.toLong()
            interpolator = NestedFragmentAnimExt.createInterpolator()
            val type =
                if (mHideAnimFromDrag) TabPanelChangeFrom.TYPE_HIDE_ANIMATION_BY_DRAG else TabPanelChangeFrom.TYPE_HIDE_ANIMATION
            addUpdateListener { updateShowedHeight(type) }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    mContent!!.isEnabled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    updateShowedHeight(type)
                    mContent!!.isEnabled = true
                    mHideAnimFromDrag = false
                    mAnimation = null
                    dismissInternal()
                }
            })
            mContent!!.isEnabled = false
            start()
        }
    }

    fun unbind() {
        mAnimation?.cancel()
        mAnimation = null
        mAnimationStarter?.let {
            it.cancel()
            mAnimationStarter = null
        }
        mActivity = null
        mContent?.apply {
            mOnTopChanged = null
            removeOnLayoutChangeListener(mListener)
            mContent = null
        }
    }

    private fun dismissInternal() {
        mFragment?.apply {
            parentFragmentManager
                .beginTransaction()
                .remove(this)
                .commitAllowingStateLoss()
        }
    }

    private fun updateShowedHeight(type: Int) {
        if (mActivity == null || mContent == null) {
            Log.e(TAG, "activity is null")
            return
        }
        //评论面板有可能离开屏幕后继续下移一段距离,此时只取屏幕高度为最大
        val fixedY = mContent?.run {
            (top.plus(translationY)).toInt().coerceAtMost(
                ViewUtil.getDisplayHeight(mActivity!!)
            )
        } ?: 0

        var progress: Float = mContent?.run {
            translationY / height.toFloat()
        } ?: 0f
        progress = 1f.coerceAtMost(progress)
        progress = 0f.coerceAtLeast(progress)
        progress = 1f - progress
        Log.d(TAG, "updateShowedHeight: progress= $progress")
    }

}