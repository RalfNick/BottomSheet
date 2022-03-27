package com.ralf.bottomsheet

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.animation.PathInterpolatorCompat

object BottomSheetAnimationUtil {

    /**
     * 覆盖Dialog的弹出动画：从底部弹出，加减速插值器。
     * 注：前提是必须关掉window动画 android:windowEnterAnimation=null
     */
    internal fun overrideDialogEnterAnimFromBottom(
        translateView: View,
        enterDuration: Long = 300L,
        listener: Animator.AnimatorListener? = null
    ) {
        translateView.viewTreeObserver.addOnPreDrawListener(object :
            ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                translateView.viewTreeObserver?.let {
                    if (it.isAlive) {
                        it.removeOnPreDrawListener(this)
                    }
                }
                val translateTo = 0f
                val translateFrom = translateView.measuredHeight.toFloat()
                translateView.translationY = translateFrom
                ValueAnimator.ofFloat(translateFrom, translateTo).apply {
                    interpolator = PathInterpolatorCompat.create(0.645f, 0.045f, 0.355f, 1f)
                    duration = enterDuration
                    addUpdateListener { animation ->
                        val animatedValue = animation.animatedValue as Float
                        translateView.translationY = animatedValue
                    }
                    if (listener != null) {
                        addListener(listener)
                    }
                    start()
                }
                return false
            }
        })
    }

}