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
 * Boolean防空
 * */
val Boolean?.orFalse get() = this ?: false

/**
 * Boolean防空
 * */
val Boolean?.orTrue get() = this ?: true

/**
 * 转Boolean
 * */
fun Any?.toBoolean(default: Boolean = false) = this as? Boolean ?: default

/**
 * 数值安全转换
 */
val <T : Number> T?.orZero: T
    get() {
        return this ?: (when (this) {
            is Short? -> 0.toShort()
            is Byte? -> 0.toByte()
            is Int? -> 0
            is Long? -> 0L
            is Double? -> 0.0
            is Float? -> 0f
            is BigDecimal? -> BigDecimal.ZERO
            else -> 0
        } as T)
    }

/**
 * 保留小数
 */
fun Number?.toFixed(fixed: Int, mode: Int = BigDecimal.ROUND_UP): String {
    return BigDecimal((this ?: 0).toString()).toFixed(fixed, mode)
}

/**
 * 保留小数
 */
fun String?.toFixed(fixed: Int, mode: Int = BigDecimal.ROUND_UP): String {
    return BigDecimal(this ?: "0").toFixed(fixed, mode)
}

/**
 * 保留小数
 */
fun BigDecimal?.toFixed(fixed: Int, mode: Int = BigDecimal.ROUND_UP): String {
    return (this ?: BigDecimal.ZERO).setScale(fixed, mode).toPlainString()
}

/**
 * 保留小数
 */
fun Number?.toFixedDouble(fixed: Int, mode: Int = BigDecimal.ROUND_UP): Double {
    return BigDecimal((this ?: 0).toString()).setScale(fixed, mode).toDouble()
}

/**
 * 当小数位超过两位时，只显示两位，但只有一位或没有，则不需要补0
 */
fun Number?.toFixedRounding() = DecimalFormat("0.##").format(this) ?: ""

/**
 * 保证小数位6位的经纬度
 */
fun Number.toFixedCompletion(length: Int = 6): String {
    var pattern = "0."
    for (index in 0 until length) {
        pattern = "${pattern}0"
    }
    return DecimalFormat(pattern).format(this)
}

/**
 * 保留小数，末尾为零则不显示0
 */
fun Number?.toFixedWithoutZero(fixed: Int, mode: Int = BigDecimal.ROUND_UP): String {
    return BigDecimal((this.orZero).toString()).setScale(fixed, mode).stripTrailingZeros().toPlainString()
}

/**
 * 去除所有0
 */
fun String.removeEndZero(): String {
    return try {
        BigDecimal(this).stripTrailingZeros().toPlainString()
    } catch (e: Exception) {
        this
    }
}

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