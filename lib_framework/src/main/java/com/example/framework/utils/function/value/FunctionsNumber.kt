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
 * 1、ROUND_UP
 * 舍入远离零的舍入模式。
 * 在丢弃非零部分之前始终增加数字(始终对非零舍弃部分前面的数字加1)。
 * 注意，此舍入模式始终不会减少计算值的大小。
 * 2、ROUND_DOWN
 * 接近零的舍入模式。
 * 在丢弃某部分之前始终不增加数字(从不对舍弃部分前面的数字加1，即截短)。
 * 注意，此舍入模式始终不会增加计算值的大小。
 * 3、ROUND_CEILING
 * 接近正无穷大的舍入模式。
 * 如果 BigDecimal 为正，则舍入行为与 ROUND_UP 相同;
 * 如果为负，则舍入行为与 ROUND_DOWN 相同。
 * 注意，此舍入模式始终不会减少计算值。
 * 4、ROUND_FLOOR
 * 接近负无穷大的舍入模式。
 * 如果 BigDecimal 为正，则舍入行为与 ROUND_DOWN 相同;
 * 如果为负，则舍入行为与 ROUND_UP 相同。
 * 注意，此舍入模式始终不会增加计算值。
 * 5、ROUND_HALF_UP
 * 向“最接近的”数字舍入，如果与两个相邻数字的距离相等，则为向上舍入的舍入模式。
 * 如果舍弃部分 >= 0.5，则舍入行为与 ROUND_UP 相同;否则舍入行为与 ROUND_DOWN 相同。
 * 注意，这是我们大多数人在小学时就学过的舍入模式(四舍五入)。
 * 6、ROUND_HALF_DOWN
 * 向“最接近的”数字舍入，如果与两个相邻数字的距离相等，则为上舍入的舍入模式。
 * 如果舍弃部分 > 0.5，则舍入行为与 ROUND_UP 相同;否则舍入行为与 ROUND_DOWN 相同(五舍六入)。
 * 7、ROUND_HALF_EVEN    银行家舍入法
 * 向“最接近的”数字舍入，如果与两个相邻数字的距离相等，则向相邻的偶数舍入。
 * 如果舍弃部分左边的数字为奇数，则舍入行为与 ROUND_HALF_UP 相同;
 * 如果为偶数，则舍入行为与 ROUND_HALF_DOWN 相同。
 * 注意，在重复进行一系列计算时，此舍入模式可以将累加错误减到最小。
 * 此舍入模式也称为“银行家舍入法”，主要在美国使用。四舍六入，五分两种情况。
 * 如果前一位为奇数，则入位，否则舍去。
 * 以下例子为保留小数点1位，那么这种舍入方式下的结果。
 * 1.15>1.2 1.25>1.2
 * 8、ROUND_UNNECESSARY
 * 断言请求的操作具有精确的结果，因此不需要舍入。
 * 如果对获得精确结果的操作指定此舍入模式，则抛出ArithmeticException。
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
    return toSafeBigDecimal().compareTo(number.toSafeBigDecimal())
}

/**
 * 加
 * number可以是Number類型轉換為字符串
 * 如果number是字符串，必須是數值（'0'或‘-1’）的字符串
 */
fun String?.add(number: String): String {
    return toSafeBigDecimal().add(number.toSafeBigDecimal()).toPlainString().removeEndZero()
}

/**
 * 減
 * number可以是Number類型轉換為字符串
 * 如果number是字符串，必須是數值（'0'或‘-1’）的字符串
 */
fun String?.subtract(number: String): String {
    return toSafeBigDecimal().subtract(number.toSafeBigDecimal()).toPlainString().removeEndZero()
}

/**
 * 乘
 * number可以是Number類型轉換為字符串
 * 如果number是字符串，必須是數值（'0'或‘-1’）的字符串
 */
fun String?.multiply(number: String): String {
    return toSafeBigDecimal().multiply(number.toSafeBigDecimal()).toPlainString().removeEndZero()
}

/**
 * 除
 * number可以是Number類型轉換為字符串
 * 如果number是字符串，必須是數值（'1'或‘-1’）的字符串
 * 如果除数是小数或除不尽，则必须指定小数位数
 */
fun String?.divide(number: String, scale: Int = 0, mode: Int = BigDecimal.ROUND_DOWN): String {
    //抹去末尾多餘的0，某些字符串可能是0.0或0.00,除數不能為0，碰到這種情況直接返回0
    if (number.toSafeBigDecimal().toPlainString().removeEndZero() == "0") return "0"
    return toSafeBigDecimal().divide(number.removeEndZero().toSafeBigDecimal(), scale, mode).toPlainString().removeEndZero()
}