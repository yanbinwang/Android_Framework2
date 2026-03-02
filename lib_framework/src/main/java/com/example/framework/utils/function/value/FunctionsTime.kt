package com.example.framework.utils.function.value

import com.example.framework.utils.function.value.DateFormat.EN_YMD
import com.example.framework.utils.function.value.DateFormat.EN_YMDHMS
import com.example.framework.utils.function.value.DateFormat.getDateFormat
import com.example.framework.utils.function.value.DateFormat.timeContrast
import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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
 * 存储 “服务器真实时间 - 本地 nano 转换的毫秒时间” 的差值，初始化 - 1 表示未同步服务器时间
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
val currentTimeNano: Long
    get() {
        return System.nanoTime() / 1000000L
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
 * 计算日期差距 (将两个时间戳都转换为 “相对于 2000 年 1 月 1 日的天数”，再做差值计算)
 * @this/other 毫秒级时间戳（从 1970-01-01 00:00:00 UTC 到某个时间的毫秒数）
 * this - other，主体时间越靠后值越大
 * 正数 -> 日程时间大于系统时间
 * 负数 -> 日程时间小于系统时间
 * 0 -> 相等
 */
fun Long?.dayDiff(other: Long?): Int {
    this ?: return 0
    other ?: return 0
    val timeDay = floor((this - timeContrast) / (1000f * 60f * 60f * 24f)).toSafeInt()
    val timeDay2 = floor((other - timeContrast) / (1000f * 60f * 60f * 24f)).toSafeInt()
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
 * 获取时间戳对应的年、月、日（支持可空Long，空值返回默认值）
 * @param defaultYear 空值时的默认年（默认2000）
 * @param defaultMonth 空值时的默认月（默认1）
 * @param defaultDay 空值时的默认日（默认1）
 * @return Triple<年, 月, 日>：月份已+1（符合日常认知，1=1月），日期从1开始
 */
fun Long?.getYearMonthDay(defaultYear: Int = 2000, defaultMonth: Int = 1, defaultDay: Int = 1): Triple<Int, Int, Int> {
    // 空值直接返回默认的年月日
    this ?: return Triple(defaultYear, defaultMonth, defaultDay)
    // 非空则解析时间戳
    val calendar = Calendar.getInstance().apply {
        // 避免lambda歧义，用this@xxx指定外层this
        time = Date(this@getYearMonthDay)
    }
    // 年：直接取；月：Calendar.MONTH从0开始，+1后符合日常认知；日：直接取
    val year = calendar.get(YEAR)
    val month = calendar.get(MONTH) + 1
    val day = calendar.get(DAY_OF_MONTH)
    return Triple(year, month, day)
}

/**
 * 是否为当日 (手机时间为准) -> 针对国内时差
 * after -> 当Date1大于Date2时，返回TRUE，当小于等于时，返回false
 * before -> 当Date1小于Date2时，返回TRUE，当大于等于时，返回false
 */
fun Date?.isToday(): Boolean {
    this ?: return false
    return try {
//        // 获取当前系统时间
//        val subDate = EN_YMD.convert(System.currentTimeMillis())
//        // 定义每天的24h时间范围
//        val beginTime = "$subDate 00:00:00"
//        val endTime = "$subDate 23:59:59"
//        // 转换Date
//        val dateFormat = EN_YMDHMS.getDateFormat()
//        val parseBeginTime = dateFormat.parse(beginTime)
//        val parseEndTime = dateFormat.parse(endTime)
//        (after(parseBeginTime) && before(parseEndTime)) || equals(parseBeginTime) || equals(parseEndTime)
        val today = Calendar.getInstance().apply {
            timeZone = TimeZone.getTimeZone("Asia/Shanghai")
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val tomorrow = today + 1.day
        val thisTime = this.time
        thisTime in today..<tomorrow
    } catch (e: ParseException) {
        e.printStackTrace()
        false
    }
}

/**
 * 假定服务器给的日期格式为：yyyy-MM-dd HH:mm:ss，通常使用US或者China，本身为零时区
 * 此时应减去手机获取到的时区，从而得到转换后的时区
 * @param this 日期(2022-12-11 22:22:22)
 * @return 将服务器返回的时间字符串解析为本地时间戳（毫秒），同时处理时区偏移问题
 */
fun String?.convertServerTime(format: String = EN_YMDHMS): Long {
    if (isNullOrEmpty()) return 0L
    // 服务器时间是UTC，先按UTC解析，再转本地时区
    val utcFormat = SimpleDateFormat(format, Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val date = utcFormat.parse(this, ParsePosition(0)) ?: return 0L
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
        format.convert(getDateFormat().parse(source))
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
    return getDateFormat().format(Date(timestamp))
}

/**
 * 传入指定日期格式和日期類转换成日期字符串
 * @param this 日期格式(yyyy-MM-dd)
 * @param date 日期类
 * @return     日期(2022-12-11)
 */
fun String.convert(date: Date?): String {
    date ?: return ""
    return getDateFormat().format(date)
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
        val comparedDate = dateFormat.parse(this) ?: Date()
        val comparedDate2 = dateFormat.parse(source) ?: Date()
        when {
            // 日程时间大于系统时间
            comparedDate.time > comparedDate2.time -> 1
            // 日程时间小于系统时间
            comparedDate.time < comparedDate2.time -> -1
            // 日程时间等于系统时间
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
            it.time = EN_YMD.getDateFormat().parse(this) ?: Date()
            it.get(Calendar.WEEK_OF_MONTH)
        }
    } catch (e: ParseException) {
        e.printStackTrace()
        0
    }
}

/**
 * 获取日期是「星期几」
 * @param this 日期（yyyy-MM-dd）
 * @return 1(周一)~7(周日)
 */
fun String?.getWeekOfDate(): Int {
    this ?: return 0
    return try {
        Calendar.getInstance(Locale.CHINA).apply {
            time = EN_YMD.getDateFormat().parse(this@getWeekOfDate) ?: Date()
        }.get(Calendar.DAY_OF_WEEK).let {
            // Calendar中：周日=1，周一=2...周六=7 → 转换为：周一=1，周日=7
            if (it == Calendar.SUNDAY) 7 else it - 1
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

    /**
     * 获取手机计算日历
     * 通过 Calendar 类获取一个固定时间点（2000 年 1 月 1 日 0 点 0 分 0 秒）的毫秒时间戳
     */
    val timeContrast by lazy {
        Calendar.getInstance().let {
            it.set(2000, 0, 1, 0, 0, 0)
            it.timeInMillis
        }
    }

    /**
     * 缓存本地创建的日期格式（频繁创建SimpleDateFormat进行日期转换过于耗费内存）
     */
    private val formattersCache by lazy { ConcurrentHashMap<String, SimpleDateFormat>() }
    private val formatterThreadLocal = ThreadLocal<MutableMap<String, SimpleDateFormat>>()

    /**
     * 获取手机本身日期格式，指定为国内时区，避免用户手动改时区 (SimpleDateFormat 是线程不安全的)
     * @param this 日期格式（yyyy-MM-dd）
     */
    @JvmStatic
    fun String.getDateFormat(): SimpleDateFormat {
        val cache = formatterThreadLocal.get() ?: mutableMapOf<String, SimpleDateFormat>().also {
            formatterThreadLocal.set(it)
        }
        return cache.getOrPut(this) {
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
        formatterThreadLocal.remove()
    }

    /**
     * 清理指定格式的缓存（若不再需要特定格式）
     */
    @JvmStatic
    fun removeCachedFormat(formatPattern: String) {
        formattersCache.remove(formatPattern)
    }

    /**
     * 计算并设置服务器时间差
     * 1) reqStartTime：客户端发起请求的本地时间（nano 转的毫秒）；
     * 2) reqEndTime：客户端收到响应的本地时间（nano 转的毫秒）；
     * 3) serverReceiveTime：推测服务器收到请求的本地时间（取请求 / 响应时间的中间值）；
     * 4) systemTime：服务器返回的真实时间戳；
     * 5) 最终 timeDiff = 服务器真实时间 - 推测的服务器接收时间
     */
    @JvmStatic
    fun setServiceTime(reqStartTime: Long, reqEndTime: Long, systemTime: Long) {
        // 推测服务器接收请求的本地nano时间（中间值）
        val serverReceiveTime = (reqEndTime + reqStartTime) / 2
        // timeDiff = 服务器真实时间 - 推测的服务器接收时间 (reqStartTime/reqEndTime 必须是 currentTimeNano（而非System.currentTimeMillis()）)
        timeDiff = systemTime - serverReceiveTime
    }

    /**
     * 重置服务器时间差
     * 将 timeDiff 恢复为初始值 -1L，后续 currentTimeStamp 会 fallback 到 System.currentTimeMillis()
     */
    @JvmStatic
    fun resetServiceTime() {
        timeDiff = -1L
    }
}
// </editor-fold>