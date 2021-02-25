package com.dataqin.common.utils

import java.math.BigDecimal

/**
 * author:wyb
 * 计算工具类
 */
object CalculationUtil {
    private val DEF_DIV_SCALE = 10

    //加法运算
    @JvmStatic
    fun add(v1: Double, v2: Double): Double {
        val b1 = BigDecimal(v1.toString())
        val b2 = BigDecimal(v2.toString())
        return b1.add(b2).toDouble()
    }

    //减法运算
    @JvmStatic
    fun sub(v1: Double, v2: Double): Double {
        val b1 = BigDecimal(v1.toString())
        val b2 = BigDecimal(v2.toString())
        return b1.subtract(b2).toDouble()
    }

    //乘法运算
    @JvmStatic
    fun mul(v1: Double, v2: Double): Double {
        val b1 = BigDecimal(v1.toString())
        val b2 = BigDecimal(v2.toString())
        return b1.multiply(b2).toDouble()
    }

    //除法运算
    @JvmStatic
    fun div(v1: Double, v2: Double): Double {
        return div(v1, v2, DEF_DIV_SCALE)
    }

    //除法运算-当发生除不尽的情况时，由scale参数指定精度，以后的数字四舍五入。
    @JvmStatic
    fun div(v1: Double, v2: Double, scale: Int): Double {
        require(scale >= 0) { "The scale must be a positive integer or zero" }
        val b1 = BigDecimal(v1.toString())
        val b2 = BigDecimal(v2.toString())
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).toDouble()
    }

    //小数位四舍五入处理
    @JvmStatic
    fun round(v: Double, scale: Int): Double {
        require(scale >= 0) { "The scale must be a positive integer or zero" }
        val b = BigDecimal(v.toString())
        val one = BigDecimal("1")
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).toDouble()
    }

}