package com.example.common.utils

import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * author:wyb
 * 计算工具类
 * kotlin中使用其自带的方法:
 * （1）a + b -> a.plus(b)
 * （2）a - b -> a.minus(b)
 * （3）a * b -> a.times(b)
 * （4）a / b -> a.div(b)
 * （5）a % b -> a.mod(b)
 */
object CalculationUtil {

    /**
     * 加法运算
     */
    @JvmStatic
    fun plus(v1: Double, v2: Double): Double {
        val b1 = BigDecimal(v1.toString())
        val b2 = BigDecimal(v2.toString())
        return b1.add(b2).toDouble()
    }

    /**
     * 减法运算
     */
    @JvmStatic
    fun minus(v1: Double, v2: Double): Double {
        val b1 = BigDecimal(v1.toString())
        val b2 = BigDecimal(v2.toString())
        return b1.subtract(b2).toDouble()
    }

    /**
     * 乘法运算
     */
    @JvmStatic
    fun times(v1: Double, v2: Double): Double {
        val b1 = BigDecimal(v1.toString())
        val b2 = BigDecimal(v2.toString())
        return b1.multiply(b2).toDouble()
    }

    /**
     * 除法运算-当发生除不尽的情况时，由scale参数指定精度，以后的数字四舍五入
     */
    @JvmStatic
    fun div(v1: Double, v2: Double, scale: Int = 10): Double {
        require(scale >= 0) { "The scale must be a positive integer or zero" }
        val b1 = BigDecimal(v1.toString())
        val b2 = BigDecimal(v2.toString())
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).toDouble()
    }

    /**
     * 小数位四舍五入处理
     */
    @JvmStatic
    fun round(v: Double, scale: Int): Double {
        require(scale >= 0) { "The scale must be a positive integer or zero" }
        return BigDecimal(v.toString()).divide(BigDecimal("1"), scale, BigDecimal.ROUND_HALF_UP).toDouble()
    }

}

/**
 * 当小数位不超过两位时，补0
 */
fun Double.completion() = DecimalFormat("0.00").format(this) ?: ""

/**
 * 当小数位超过两位时，只显示两位，但只有一位或没有，则不需要补0
 */
fun Double.rounding() = DecimalFormat("0.##").format(this) ?: ""