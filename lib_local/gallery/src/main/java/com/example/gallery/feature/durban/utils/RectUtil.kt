package com.example.gallery.feature.durban.utils

import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * 矩形坐标计算工具类
 * 作用：裁剪视图中计算矩形、角点、中心点、包围盒等几何逻辑
 */
object RectUtil {

    /**
     * 从矩形获取四个角点坐标
     * 角点顺序：左上 → 右上 → 右下 → 左下
     */
    @JvmStatic
    fun getCornersFromRect(r: RectF): FloatArray {
        return floatArrayOf(r.left, r.top, r.right, r.top, r.right, r.bottom, r.left, r.bottom)
    }

    /**
     * 从角点坐标计算矩形的宽和高
     */
    @JvmStatic
    fun getRectSidesFromCorners(corners: FloatArray): FloatArray {
        val width = sqrt((corners[0] - corners[2]).pow(2) + (corners[1] - corners[3]).pow(2))
        val height = sqrt((corners[2] - corners[4]).pow(2) + (corners[3] - corners[5]).pow(2))
        return floatArrayOf(width, height)
    }

    /**
     * 获取矩形中心点坐标
     */
    @JvmStatic
    fun getCenterFromRect(r: RectF): FloatArray {
        return floatArrayOf(r.centerX(), r.centerY())
    }

    /**
     * 根据一组坐标点，计算能包裹它们的最小矩形
     */
    @JvmStatic
    fun trapToRect(array: FloatArray): RectF {
        val r = RectF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
        var i = 1
        while (i < array.size) {
            val x = (array[i - 1] * 10).roundToInt() / 10f
            val y = (array[i] * 10).roundToInt() / 10f
            r.left = min(x, r.left)
            r.top = min(y, r.top)
            r.right = max(x, r.right)
            r.bottom = max(y, r.bottom)
            i += 2
        }
        r.sort()
        return r
    }

}