package com.example.framework.utils.function.value

import com.example.framework.utils.function.value.DateFormat.EN_YMD
import com.example.framework.utils.function.value.DateFormat.EN_YMDHMS
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

//------------------------------------日期时间工具类------------------------------------
/**
 * 现在的运行时间，用来作时间间隔判断
 */
val currentTimeNano: Long
    get() {
        return System.nanoTime() / 1000000L
    }

/**
 * 获取毫秒值
 */
val Int.hour get() = this * 1000L * 60L * 60L
val Int.minute get() = this * 1000L * 60L
val Int.second get() = this * 1000L
val Int.day get() = this * 1000L * 60L * 60L * 24L
val Int.week get() = this * 1000L * 60L * 60L * 24L * 7L

/**
 * 日期形式字符串
 */
object DateFormat {
    const val EN_M = "MM"
    const val EN_MD = "MM-dd"
    const val EN_HM = "HH:mm"
    const val EN_HMS = "HH:mm:ss"
    const val EN_YM = "yyyy-MM"
    const val EN_YMD = "yyyy-MM-dd"
    const val EN_YMDHM = "yyyy-MM-dd HH:mm"
    const val EN_YMDHMS = "yyyy-MM-dd HH:mm:ss"
    const val CN_M = "M月"
    const val CN_MD = "M月d日"
    const val CN_HM = "HH时mm分"
    const val CN_HMS = "HH时mm分ss秒"
    const val CN_YM = "yyyy年M月"
    const val CN_YMD = "yyyy年MM月dd日"
    const val CN_YMDHM = "yyyy年MM月dd日 HH时mm分"
    const val CN_YMDHMS = "yyyy年MM月dd日 HH时mm分ss秒"
}

/**
 * 日期对比（统一年月日形式）
 * @param this       比较日期a
 * @param toSource   比较日期b
 */
@Synchronized
fun String.compareDate(toSource: String, format: String = EN_YMD): Int {
    val dateFormat = format.getDateFormat()
    try {
        val comparedDate = dateFormat.parse(this) ?: Date()
        val comparedDate2 = dateFormat.parse(toSource) ?: Date()
        return when {
            comparedDate.time > comparedDate2.time -> 1//日程时间大于系统时间
            comparedDate.time < comparedDate2.time -> -1//日程时间小于系统时间
            else -> 0
        }
    } catch (_: Exception) {
    }
    return 0
}

/**
 * 获取转换日期
 * @param this       被转换的日期格式
 * @param toFormat   要转换的日期格式
 * @param source     被转换的日期
 */
@Synchronized
fun String.getDateFormat(toFormat: String, source: String): String {
    var result = ""
    try {
        result = toFormat.getDateTime(getDateFormat().parse(source) ?: Date())
    } catch (_: ParseException) {
    }
    return result
}

/**
 * 传入指定格式的日期字符串转成毫秒
 * @param this   日期格式
 * @param source 日期
 */
@Synchronized
fun String.getDateTime(source: String) = getDateFormat().parse(source)?.time ?: 0

/**
 * 传入指定日期格式和毫秒转换成日期字符串
 * @param this 日期格式
 * @param timestamp 时间戳
 */
@Synchronized
fun String.getDateTime(timestamp: Long) = getDateFormat().format(Date(timestamp)) ?: ""

/**
 * 传入指定日期格式和日期類转换成日期字符串
 * @param this 日期格式
 * @param date 日期类
 */
@Synchronized
fun String.getDateTime(date: Date) = getDateFormat().format(date) ?: ""

/**
 * 获取日期的当月的第几周
 * @param this 日期（yyyy-MM-dd）
 */
@Synchronized
fun String.getWeekOfMonth(): Int {
    try {
        val source = this
        Calendar.getInstance().apply {
            time = EN_YMD.getDateFormat().parse(source) ?: Date()
            return get(Calendar.WEEK_OF_MONTH)
        }
    } catch (_: ParseException) {
    }
    return 0
}

/**
 * 获取日期是第几周
 * @param source 日期（yyyy-MM-dd）
 */
@Synchronized
fun String.getWeekOfDate(): Int {
    try {
        val source = this
        Calendar.getInstance().apply {
            time = EN_YMD.getDateFormat().parse(source) ?: Date()
            var weekIndex = get(Calendar.DAY_OF_WEEK) - 1
            if (weekIndex < 0) weekIndex = 0
            return weekIndex
        }
    } catch (_: ParseException) {
    }
    return 0
}

/**
 * 返回中文形式的星期
 * @param source 日期（yyyy-MM-dd）
 */
@Synchronized
fun String.getDateWeek(): String {
    return when (getWeekOfDate()) {
        0 -> "星期天"
        1 -> "星期一"
        2 -> "星期二"
        3 -> "星期三"
        4 -> "星期四"
        5 -> "星期五"
        6 -> "星期六"
        else -> ""
    }
}

/**
 * 获取日期格式，时区为校准的中国时区
 * @param format 日期格式
 */
private fun String.getDateFormat(): SimpleDateFormat {
    val dateFormat = SimpleDateFormat(this, Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    return dateFormat
}

/**
 * 是否为当日
 */
@Synchronized
fun Date.isToday(): Boolean {
    var flag = false
    try {
        //获取当前系统时间
        val subDate = EN_YMD.getDateTime(System.currentTimeMillis())
        //定义每天的24h时间范围
        val beginTime = "$subDate 00:00:00"
        val endTime = "$subDate 23:59:59"
        //转换Date
        val dateFormat = EN_YMDHMS.getDateFormat()
        val parseBeginTime = dateFormat.parse(beginTime)
        val parseEndTime = dateFormat.parse(endTime)
        if (after(parseBeginTime) && before(parseEndTime)) flag = true
    } catch (_: ParseException) {
    }
    return flag
}

/**
 * 传入毫秒转换成00:00的格式
 * @param this 时间戳
 */
@Synchronized
fun Long.getTime(): String {
    if (this <= 0) return "00:00"
    val second = (this / 1000 / 60).toInt()
    val million = (this / 1000 % 60).toInt()
    return "${if (second >= 10) second.toString() else "0$second"}:${if (million >= 10) million.toString() else "0$million"}"
}

/**
 * 处理时间
 * @param this 时间戳->秒
 */
@Synchronized
fun Long.getSecondFormat(): String {
    val result: String?
    val hour: Long
    val second: Long
    var minute: Long
    if (this <= 0) return "00:00" else {
        minute = this / 60
        if (minute < 60) {
            second = this % 60
            result = "${minute.unitFormat()}:${second.unitFormat()}"
        } else {
            hour = minute / 60
            if (hour > 99) return "99:59:59"
            minute %= 60
            second = this - hour * 3600 - minute * 60
            result = "${hour.unitFormat()}:${minute.unitFormat()}:${second.unitFormat()}"
        }
    }
    return result
}

private fun Long.unitFormat() = if (this in 0..9) "0$this" else this.toString()