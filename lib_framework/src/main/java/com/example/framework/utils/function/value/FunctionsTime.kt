package com.example.framework.utils.function.value

import com.example.framework.utils.function.value.DateFormat.EN_YMD
import com.example.framework.utils.function.value.DateFormat.EN_YMDHMS
import com.example.framework.utils.function.value.DateFormat.getDateFormat
import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.floor

//------------------------------------日期时间工具类------------------------------------

/**
 * 获取毫秒值
 */
val Int.hour get() = this * 1000L * 60 * 60  // 1小时 = 3600000毫秒
val Int.minute get() = this * 1000L * 60     // 1分钟 = 60000毫秒
val Int.second get() = this * 1000L          // 1秒 = 1000毫秒
val Int.day get() = this * 24.hour           // 1天 = 24小时（复用hour扩展）
val Int.week get() = this * 7.day            // 1周 = 7天（复用day扩展）

/**
 * 服务器时间-推测的服务器接收时间
 */
private var timeDiff = -1L

/**
 * 现在的时间戳
 */
val currentTimeStamp: Long
    get() {
        return if (timeDiff < 0) {
            System.currentTimeMillis()
        } else {
            currentTimeNano + timeDiff
        }
    }

/**
 * 以纳秒为单位，返回正在运行的Java虚拟机的高分辨率时间源的当前值
 * 该方法只能用于测量经过时间，与系统或挂钟时间的任何其他概念无关，用来作时间间隔判断
 * 1秒 = 10分秒；
 * 1分秒 = 10厘秒；
 * 1厘秒 = 10毫秒；
 * 1毫秒 = 1000微秒；-》取得的是毫秒
 * 1微秒 = 1000纳秒；
 * 1纳秒 = 1000皮秒；
 */
val currentTimeNano get() = System.nanoTime() / 1000000L

/**
 * 获取手机计算日历
 */
val timeContrast by lazy {
    Calendar.getInstance().let {
        it.set(2000, 0, 1, 0, 0, 0)
        it.timeInMillis
    }
}

/**
 * 是否为今天
 */
val Long?.isToday: Boolean
    get() {
        this ?: return false
        return this.dayDiff(currentTimeStamp) == 0
    }

/**
 * 计算日期差距
 * this - other，主体时间越靠后值越大
 * 正数->日程时间大于系统时间
 * 负数->日程时间小于系统时间
 * 0->相等
 */
fun Long?.dayDiff(other: Long?): Int {
    this ?: return 0
    other ?: return 0
    val timeDay = floor((this - timeContrast) / (1000f * 60f * 60f * 24f)).toInt()
    val timeDay2 = floor((other - timeContrast) / (1000f * 60f * 60f * 24f)).toInt()
    return timeDay - timeDay2
}

/**
 * 时间戳差值，换算倒计时时间（00:00:00）时间单位->毫秒
 * @param showMin 显示分钟和秒钟
 * @param showSec 显示秒钟
 * */
fun Long?.formatAsCountdown(showMin: Boolean = true, showSec: Boolean = true): String {
    this ?: return when {
        showMin && showSec -> "00:00:00"
        showMin && !showSec -> "00:00"
        else -> "00"
    }
    val hour = this / 1.hour
    if (!showMin) return hour.toString()
    val minute = (this % 1.hour) / 1.minute
    if (!showSec) return "${hour.padZero(false)}:${minute.padZero(true)}"
    val second = (this % 1.minute) / 1.second
    return "${hour.padZero(false)}:${minute.padZero(true)}:${second.padZero(true)}"
}

fun Long?.formatAsCountdownCN(): String {
    var list = formatAsCountdown().split(":")
    if (list.safeSize != 3) list = listOf("00", "00", "00")
    return "${list.safeGet(0)}时${list.safeGet(1)}分${list.safeGet(2)}秒"
}

private fun Long.padZero(cap: Boolean = true): String {
    return when {
        this <= 0 -> "00"
        this < 10 -> "0$this"
        this > 99 && cap -> "99"
        else -> this.toString()
    }
}

/**
 * 获取年月
 */
fun Long.getYearAndMonth(): Pair<Int, Int> {
    val calendar = Calendar.getInstance()
    calendar.time = Date(this)
    return calendar.get(YEAR) to (calendar.get(MONTH) + 1)
}

/**
 * 转换日期如果为空，则new一个当前手机的日期类返回
 */
fun Date?.toSafeDate(): Date {
    this ?: return Date()
    return this
}

/**
 * 是否为当日(手机时间为准)->针对国内时差
 * after->当Date1大于Date2时，返回TRUE，当小于等于时，返回false
 * before->当Date1小于Date2时，返回TRUE，当大于等于时，返回false
 */
fun Date.isToday(): Boolean {
    var flag = false
    try {
        //获取当前系统时间
        val subDate = EN_YMD.convert(System.currentTimeMillis())
        //定义每天的24h时间范围
        val beginTime = "$subDate 00:00:00"
        val endTime = "$subDate 23:59:59"
        //转换Date
        val dateFormat = EN_YMDHMS.getDateFormat()
        val parseBeginTime = dateFormat.parse(beginTime)
        val parseEndTime = dateFormat.parse(endTime)
        if ((after(parseBeginTime) && before(parseEndTime)) || equals(parseBeginTime) || equals(parseEndTime)) flag = true
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return flag
}

/**
 * 假定服务器给的日期格式为：yyyy-MM-dd HH:mm:ss，通常使用US或者China，本身为零时区
 * 此时应减去手机获取到的时区，从而得到转换后的时区
 * @param this 日期(2022-12-11 22:22:22)
 * @return 将服务器返回的时间字符串解析为本地时间戳（毫秒），同时处理时区偏移问题
 */
fun String?.convertServerTime(format: String = EN_YMDHMS): Long {
    if (isNullOrEmpty()) return 0
    val date = SimpleDateFormat(format, Locale.US).parse(this, ParsePosition(0)) ?: return 0
    return date.time - date.timezoneOffset * 60000
}

/**
 * 获取转换日期
 * @param this     被转换的日期格式(yyyy-MM-dd)
 * @param format   要转换的日期格式(yyyy-MM-dd HH:mm:ss)
 * @param source   被转换的日期(2022-12-11)
 * @return         被转换的日期(2022-12-11 00:00:00)
 */
fun String?.convert(format: String, source: String): String {
    this ?: return ""
    return try {
        format.convert(getDateFormat().parse(source).toSafeDate())
    } catch (e: ParseException) {
        e.printStackTrace()
        ""
    }
}

/**
 * 传入指定格式的日期字符串转成毫秒
 * @param this   日期格式(yyyy-MM-dd)
 * @param source 日期(2022-12-11)
 * @return       时间戳
 */
fun String.convert(source: String): Long {
    return getDateFormat().parse(source)?.time.orZero
}

/**
 * 传入指定日期格式和毫秒转换成日期字符串
 * @param this      日期格式(yyyy-MM-dd)
 * @param timestamp 时间戳
 * @return          日期(2022-12-11)
 */
fun String.convert(timestamp: Long): String {
    return getDateFormat().format(Date(timestamp)).orEmpty()
}

/**
 * 传入指定日期格式和日期類转换成日期字符串
 * @param this 日期格式(yyyy-MM-dd)
 * @param date 日期类
 * @return     日期(2022-12-11)
 */
fun String.convert(date: Date): String {
    return getDateFormat().format(date).orEmpty()
}

/**
 * 日期对比（统一年月日形式）
 * @param this     比较日期a(2022-12-11 11:11:11)
 * @param source   比较日期b(2022-12-12 11:11:11)
 * @param format   比较日期的格式(a和b要一致->yyyy-MM-dd HH:mm:ss)
 * @return         1（大于）-1（小于） 0（等于）
 */
fun String?.compare(source: String, format: String = EN_YMD): Int {
    this ?: return 0
    val dateFormat = format.getDateFormat()
    return try {
        val comparedDate = dateFormat.parse(this).toSafeDate()
        val comparedDate2 = dateFormat.parse(source).toSafeDate()
        when {
            comparedDate.time > comparedDate2.time -> 1//日程时间大于系统时间
            comparedDate.time < comparedDate2.time -> -1//日程时间小于系统时间
            else -> 0
        }
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}

/**
 * 获取日期的当月的第几周
 * @param this 日期（yyyy-MM-dd）
 * @return     星期数（记得+1）
 */
fun String?.getWeekOfMonth(): Int {
    this ?: return 0
    return try {
        Calendar.getInstance().let {
            it.time = EN_YMD.getDateFormat().parse(this).toSafeDate()
            it.get(Calendar.WEEK_OF_MONTH)
        }
    } catch (e: ParseException) {
        e.printStackTrace()
        0
    }
}

/**
 * 获取日期是第几周
 * @param source 日期（yyyy-MM-dd）
 * @return       周数（记得+1）
 */
fun String?.getWeekOfDate(): Int {
    this ?: return 0
    return try {
        Calendar.getInstance().let {
            it.time = EN_YMD.getDateFormat().parse(this).toSafeDate()
            var weekIndex = it.get(Calendar.DAY_OF_WEEK) - 1
            if (weekIndex < 0) weekIndex = 0
            weekIndex
        }
    } catch (e: ParseException) {
        e.printStackTrace()
        0
    }
}

// <editor-fold defaultstate="collapsed" desc="常用的日期格式及方法">
object DateFormat {
    /**
     * 常用的一些日期格式
     */
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

    /**
     * 缓存本地创建的日期格式（频繁创建SimpleDateFormat进行日期转换过于耗费内存）
     */
    private val formattersCache by lazy { ConcurrentHashMap<String, SimpleDateFormat>() }

    /**
     * 获取手机本身日期格式，指定为国内时区，避免用户手动改时区
     * @param this 日期格式（yyyy-MM-dd）
     */
    @JvmStatic
    fun String.getDateFormat(): SimpleDateFormat {
//        val dateFormat = SimpleDateFormat(this, Locale.getDefault())
//        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
//        return dateFormat
        return formattersCache.getOrPut(this) {
            SimpleDateFormat(this, Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("Asia/Shanghai")
            }
        }
    }

    /**
     * 清理当前线程的所有日期格式缓存
     * 建议在长时间运行的后台任务结束时或Activity/Fragment销毁时调用
     */
    @JvmStatic
    fun clearThreadLocalCache() {
        formattersCache.clear()
    }

    /**
     * 清理指定格式的缓存（若不再需要特定格式）
     */
    @JvmStatic
    fun removeCachedFormat(formatPattern: String) {
        formattersCache.remove(formatPattern)
    }
}
// </editor-fold>