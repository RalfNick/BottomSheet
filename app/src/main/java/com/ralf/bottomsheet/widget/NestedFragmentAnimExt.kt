package com.ralf.bottomsheet.widget

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Interpolator

internal class NestedFragmentAnimExt {

    companion object {
        @JvmStatic
        fun createInterpolator(): Interpolator {
            val interpolator = MultiCubicInterpolator()
            //curve=[0.118,0.049,0.327,0.348,0.440,0.651,0.550,0.954,0.678,0.986]&duration=0.4
            interpolator.add(0f, 0f, 0.118f, 0.049f, 0.327f, 0.348f, 0.440f, 0.651f)
            interpolator.add(0.440f, 0.651f, 0.550f, 0.954f, 0.678f, 0.986f, 1f, 1f)
            return interpolator
        }
    }
}

internal class AnimationStarter internal constructor(
    var mContentView: View,
    animator: ValueAnimator
) :
    ViewTreeObserver.OnPreDrawListener {
    private var mViewTreeObserver: ViewTreeObserver = mContentView.viewTreeObserver
    var mValueAnimator: ValueAnimator
    var mIsCancel = true
    internal fun start() {
        mIsCancel = false
        mContentView.invalidate()
    }

    internal fun cancel() {
        mIsCancel = true
        mViewTreeObserver.removeOnPreDrawListener(this)
    }

    override fun onPreDraw(): Boolean {
        mViewTreeObserver.removeOnPreDrawListener(this)
        if (!mIsCancel) {
            mValueAnimator.start()
        }
        return true
    }

    init {
        mViewTreeObserver.addOnPreDrawListener(this)
        mValueAnimator = animator
    }
}