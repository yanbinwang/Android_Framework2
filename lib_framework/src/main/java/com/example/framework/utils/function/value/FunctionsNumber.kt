package com.example.framework.utils.function.value

import java.math.BigDecimal
import java.text.DecimalFormat

//------------------------------------计算工具类------------------------------------
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

/**
 * 设定最小值
 */
fun Int?.min(min: Int): Int {
    return when {
        this == null -> min
        this <= min -> min
        else -> this
    }
}

fun Float?.min(min: Float): Float {
    return when {
        this == null -> min
        this <= min -> min
        else -> this
    }
}

fun Double?.min(min: Double): Double {
    return when {
        this == null -> min
        this <= min -> min
        else -> this
    }
}

fun Long?.min(min: Long): Long {
    return when {
        this == null -> min
        this <= min -> min
        else -> this
    }
}

fun String?.min(min: String?): String {
    val minValue = if(min.isNullOrEmpty()) "0" else min
    return when {
        this == null -> minValue
        minValue.numberCompareTo(this).let {
            it == 0 || it == 1
        } -> minValue
        else -> this
    }
}

/**
 * 设定最大值
 */
fun Int?.max(max: Int): Int {
    return when {
        this == null -> max
        this >= max -> max
        else -> this
    }
}

fun Long?.max(max: Long): Long {
    return when {
        this == null -> max
        this >= max -> max
        else -> this
    }
}

fun Float?.max(max: Float): Float {
    return when {
        this == null -> max
        this >= max -> max
        else -> this
    }
}

fun Double?.max(max: Double): Double {
    return when {
        this == null -> max
        this >= max -> max
        else -> this
    }
}

fun String?.max(max: String?): String {
    val maxValue = if(max.isNullOrEmpty()) "0" else max
    return when {
        this == null -> maxValue
        this.numberCompareTo(maxValue).let {
            it == 0 || it == 1
        } -> maxValue
        else -> this
    }
}

/**
 * 设定范围
 */
fun Int?.fitRange(range: IntRange): Int {
    return when {
        this == null -> range.first
        this <= range.first -> range.first
        this >= range.last -> range.last
        else -> this
    }
}

/**
 * 设定范围
 */
fun Long?.fitRange(range: LongRange): Long {
    return when {
        this == null -> range.first
        this <= range.first -> range.first
        this >= range.last -> range.last
        else -> this
    }
}

/**
 * 设定范围
 */
fun Double?.fitRange(range: IntRange): Double {
    return when {
        this == null -> range.first.toDouble()
        this <= range.first -> range.first.toDouble()
        this >= range.last -> range.last.toDouble()
        else -> this
    }
}

/**
 * 设定范围
 */
fun Float?.fitRange(range: IntRange): Float {
    return when {
        this == null -> range.first.toFloat()
        this <= range.first -> range.first.toFloat()
        this >= range.last -> range.last.toFloat()
        else -> this
    }
}

/**
 * 保留fixed位小数
 * double a = 1.66728D;
 * double b = 1.33333D;
 * double c = 1.00000D;
 * aa.setScale(2, BigDecimal.ROUND_UP)
 * aa.setScale(2, BigDecimal.ROUND_DOWN)
 * bb.setScale(2, BigDecimal.ROUND_UP)
 * bb.setScale(2, BigDecimal.ROUND_DOWN)
 * cc.setScale(2, BigDecimal.ROUND_UP)
 * cc.setScale(2, BigDecimal.ROUND_DOWN)
 * 1.67
 * 1.66
 * 1.34
 * 1.33
 * 1.00
 * 1.00
 * var price: BigDecimal = BigDecimal.ZERO->使用BigDecimal接取过长小数点的价格
 */
fun Number?.toFixed(fixed: Int, mode: Int = BigDecimal.ROUND_DOWN): String {
    return BigDecimal((this ?: 0).toString()).toFixed(fixed, mode)
}

/**
 * 保留fixed位小数
 * 后端如果数值过大是不能用double接取的，使用string接受转BigDecimal，或直接BigDecimal接取
 */
fun String?.toFixed(fixed: Int, mode: Int = BigDecimal.ROUND_DOWN): String {
    return BigDecimal(this ?: "0").toFixed(fixed, mode)
}

/**
 * 保留fixed位小数
 */
fun BigDecimal?.toFixed(fixed: Int, mode: Int = BigDecimal.ROUND_DOWN): String {
    return (this ?: BigDecimal.ZERO).setScale(fixed, mode).toPlainString()
}

/**
 * 保证小数位X位,不做四舍五入
 * val a = 1.6672; fixed=2
 * ->1.66
 * val b = 1.6672; fixed=5
 * ->1.66720
 * -------------------------
 * val a = 1.667; fixed=2
 * ->1.66
 * val b = 1.6; fixed=2
 * ->1.6
 * -------------------------
 * '0'->会补
 * '#'->不会补
 */
fun Number.toFixed(fixed: Int = 1, replenish: Boolean = true): String {
    val format = StringBuffer("0.")
    for (i in 0 until fixed) {
        format.append(if(replenish) "0" else "#")
    }
    return DecimalFormat(format.toString()).format(this) ?: "0"
}

/**
 * 保留小数，末尾为零则不显示0
 * 1.0000000->1
 * 1.0003300->1.00033
 */
fun Number?.toFixedWithoutZero(fixed: Int, mode: Int = BigDecimal.ROUND_DOWN): String {
    return BigDecimal((this.orZero).toString()).setScale(fixed, mode).stripTrailingZeros().toPlainString()
}

/**
 * 去除所有小数的0
 * 1.0000000->1
 */
fun String?.removeEndZero(): String {
    this ?: return "0"
    return try {
        BigDecimal(this).stripTrailingZeros().toPlainString()
    } catch (e: Exception) {
        this
    }
}

/**
 * 获取小数位
 */
fun String?.numberDigits(): Int {
    this ?: return 0
    val list = this.split(".")
    return if(list.size > 1) list.safeGet(1)?.length.orZero else 0
}

/**
 * val a= bd1.compareTo(bd2)
 * a = -1,表示bd1小于bd2
 * a = 0,表示bd1等于bd2
 * a = 1,表示bd1大于bd2
 */
fun String?.numberCompareTo(number: String): Int {
    return BigDecimal(this).compareTo(BigDecimal(number))
}

/**
 * 加
 * number可以是Number類型轉換為字符串
 * 如果number是字符串，必須是數值（'0'或‘-1’）的字符串
 */
fun String?.add(number: String): String {
    return BigDecimal(this).add(BigDecimal(number)).toPlainString().removeEndZero()
}

/**
 * 減
 * number可以是Number類型轉換為字符串
 * 如果number是字符串，必須是數值（'0'或‘-1’）的字符串
 */
fun String?.subtract(number: String): String {
    return BigDecimal(this).subtract(BigDecimal(number)).toPlainString().removeEndZero()
}

/**
 * 乘
 * number可以是Number類型轉換為字符串
 * 如果number是字符串，必須是數值（'0'或‘-1’）的字符串
 */
fun String?.multiply(number: String): String {
    return BigDecimal(this).multiply(BigDecimal(number)).toPlainString().removeEndZero()
}

/**
 * 除
 * number可以是Number類型轉換為字符串
 * 如果number是字符串，必須是數值（'1'或‘-1’）的字符串
 * 如果除数是小数或除不尽，则必须指定小数位数
 */
fun String?.divide(number: String, scale: Int = 0, mode: Int = BigDecimal.ROUND_DOWN): String {
    //抹去末尾多餘的0，某些字符串可能是0.0或0.00,除數不能為0，碰到這種情況直接返回0
    if (number.removeEndZero() == "0") return "0"
    return BigDecimal(this).divide(BigDecimal(number.removeEndZero()), scale, mode).toPlainString().removeEndZero()
}