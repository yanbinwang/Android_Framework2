package com.example.base.utils.function

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
//------------------------------------计算工具类------------------------------------
/**
 * 加法运算
 */
fun String.add(v: String) = BigDecimal(this).add(BigDecimal(v)).toDouble()

fun String.add(v: Double) = BigDecimal(this).add(BigDecimal(v)).toDouble()

fun Double.add(v: String) = BigDecimal(this).add(BigDecimal(v)).toDouble()

fun Double.add(v: Double) = BigDecimal(this).add(BigDecimal(v)).toDouble()

/**
 * 减法运算
 */
fun String.subtract(v: String) = BigDecimal(this).subtract(BigDecimal(v)).toDouble()

fun String.subtract(v: Double) = BigDecimal(this).subtract(BigDecimal(v)).toDouble()

fun Double.subtract(v: String) = BigDecimal(this).subtract(BigDecimal(v)).toDouble()

fun Double.subtract(v: Double) = BigDecimal(this).subtract(BigDecimal(v)).toDouble()

/**
 * 乘法运算
 */
fun String.multiply(v: String) = BigDecimal(this).multiply(BigDecimal(v)).toDouble()

fun String.multiply(v: Double) = BigDecimal(this).multiply(BigDecimal(v)).toDouble()

fun Double.multiply(v: String) = BigDecimal(this).multiply(BigDecimal(v)).toDouble()

fun Double.multiply(v: Double) = BigDecimal(this).multiply(BigDecimal(v)).toDouble()

/**
 * 除法运算-当发生除不尽的情况时，由scale参数指定精度，以后的数字四舍五入
 */
fun String.divide(v: String, scale: Int = 10) = BigDecimal(this).divide(BigDecimal(v), scale, BigDecimal.ROUND_HALF_UP).toDouble()

fun String.divide(v: Double, scale: Int = 10) = BigDecimal(this).divide(BigDecimal(v), scale, BigDecimal.ROUND_HALF_UP).toDouble()

fun Double.divide(v: String, scale: Int = 10) = BigDecimal(this).divide(BigDecimal(v), scale, BigDecimal.ROUND_HALF_UP).toDouble()

fun Double.divide(v: Double, scale: Int = 10) = BigDecimal(this).divide(BigDecimal(v), scale, BigDecimal.ROUND_HALF_UP).toDouble()

/**
 * 小数位四舍五入处理
 */
fun String.divide(scale: Int) = BigDecimal(this).divide(BigDecimal("1"), scale, BigDecimal.ROUND_HALF_UP).toDouble()

fun Double.divide(scale: Int) = BigDecimal(this).divide(BigDecimal("1"), scale, BigDecimal.ROUND_HALF_UP).toDouble()

/**
 * 当小数位不超过两位时，补0
 */
fun Double.completion() = DecimalFormat("0.00").format(this) ?: ""

/**
 * 当小数位超过两位时，只显示两位，但只有一位或没有，则不需要补0
 */
fun Double.rounding() = DecimalFormat("0.##").format(this) ?: ""

/**
 * 防空转换Int
 */
fun CharSequence?.toSafeInt(default: Int = 0): Int {
    if (this.isNullOrEmpty()) return default
    return try {
        if (this is String) {
            this.toInt()
        } else {
            this.toString().toInt()
        }
    } catch (e: Exception) {
        default
    }
}

/**
 * 防空转换Int
 */
fun Number?.toSafeInt(default: Int = 0): Int {
    this ?: return default
    return this.toInt()
}


/**
 * 防空转换Long
 */
fun CharSequence?.toSafeLong(default: Long = 0L): Long {
    if (this.isNullOrEmpty()) return default
    return try {
        if (this is String) {
            this.toLong()
        } else {
            this.toString().toLong()
        }
    } catch (e: Exception) {
        default
    }
}

/**
 * 防空转换Long
 */
fun Number?.toSafeLong(default: Long = 0L): Long {
    this ?: return default
    return this.toLong()
}

/**
 * 防空转换Double
 */
fun CharSequence?.toSafeDouble(default: Double = 0.0): Double {
    if (this.isNullOrEmpty() || this == ".") return default
    return try {
        if (this is String) {
            this.toDouble()
        } else {
            this.toString().toDouble()
        }
    } catch (e: Exception) {
        default
    }
}

/**
 * 防空转换Double
 */
fun Number?.toSafeDouble(default: Double = 0.0): Double {
    this ?: return default
    return this.toDouble()
}

/**
 * 防空转换Float
 */
fun CharSequence?.toSafeFloat(default: Float = 0f): Float {
    if (this.isNullOrEmpty() || this == ".") return default
    return try {
        if (this is String) {
            this.toFloat()
        } else {
            this.toString().toFloat()
        }
    } catch (e: Exception) {
        default
    }
}

/**
 * 防空转换Float
 */
fun Number?.toSafeFloat(default: Float = 0f): Float {
    this ?: return default
    return this.toFloat()
}

/**
 * 防空转换BigDecimal
 */
fun CharSequence?.toSafeBigDecimal(default: Double = 0.0): BigDecimal {
    if (this.isNullOrEmpty() || this == ".") return BigDecimal.valueOf(default)
    return try {
        if (this is String) {
            this.toBigDecimal()
        } else {
            this.toString().toBigDecimal()
        }
    } catch (e: Exception) {
        BigDecimal.valueOf(default)
    }
}

/**
 * 防空转换BigDecimal
 */
fun Number?.toSafeBigDecimal(default: BigDecimal = BigDecimal.ZERO): BigDecimal {
    this ?: return default
    return this.toDouble().toBigDecimal()
}