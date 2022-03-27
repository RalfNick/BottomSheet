package com.ralf.bottomsheet.widget

import android.view.animation.Interpolator

/** 多段三次贝塞尔曲线拼接 */
class MultiCubicInterpolator : Interpolator {

    private companion object {
        private const val ACCURACY = 4096

        /**
         * 求三次贝塞尔曲线(四个控制点)一个点某个维度的值.<br></br>
         *
         *
         * 参考资料: * http://devmag.org.za/2011/04/05/bzier-curves-a-tutorial/ *
         *
         * @param t      取值[0, 1]
         * @param value0
         * @param value1
         * @param value2
         * @param value3
         * @return
         */
        @JvmStatic
        fun cubicCurves(
            t: Double, value0: Double, value1: Double,
            value2: Double, value3: Double
        ): Double {
            var value: Double
            val u = 1 - t
            val tt = t * t
            val uu = u * u
            val uuu = uu * u
            val ttt = tt * t
            value = uuu * value0
            value += 3 * uu * t * value1
            value += 3 * u * tt * value2
            value += ttt * value3
            return value
        }
    }

    private val mEaseCubics: MutableList<EaseCubic> = ArrayList()

    override fun getInterpolation(input: Float): Float {
        var startX = 0f
        var startY = 0f
        for (i in mEaseCubics.indices) {
            val ec = mEaseCubics[i]
            if (input <= startX + ec.mxRang) {
                return startY + ec[(input - startX) / ec.mxRang] * ec.myRang
            } else {
                startX += ec.mxRang
                startY += ec.myRang
            }
        }
        return input
    }

    fun add(
        sx: Float,
        sy: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        ex: Float,
        ey: Float
    ) {
        val x1m = (x1 - sx) * 1f / (ex - sx)
        val x2m = (x2 - sx) * 1f / (ex - sx)
        val y1m = (y1 - sy) * 1f / (ey - sy)
        val y2m = (y2 - sy) * 1f / (ey - sy)
        mEaseCubics.add(EaseCubic(x1m, y1m, x2m, y2m, ex - sx, ey - sy))
    }

    private class EaseCubic(
        private val x1: Float,
        private val y1: Float,
        private val x2: Float,
        private val y2: Float,
        val mxRang: Float,
        val myRang: Float
    ) {
        private var mLastI = 0
        operator fun get(input: Float): Float {
            var t = input

            // 近似求解t的值[0,1]
            for (i in mLastI until ACCURACY) {
                t = 1.0f * i / ACCURACY
                val x: Double = cubicCurves(t.toDouble(), 0.0, x1.toDouble(), x2.toDouble(), 1.0)
                if (x >= input) {
                    mLastI = i
                    break
                }
            }
            var value: Double = cubicCurves(t.toDouble(), 0.0, y1.toDouble(), y2.toDouble(), 1.0)
            if (value > 0.999) {
                value = 1.0
                mLastI = 0
            }
            return value.toFloat()
        }
    }

}