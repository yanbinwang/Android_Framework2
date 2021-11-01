package com.example.base.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * author: wyb
 * date: 2018/2/7.
 * 日期格式为特殊格式,后台和前端不能直接以date变量去接取
 * 需要进行字符串转换后才能使用
 */
object DateUtil {
    //后台一般会返回的日期形式的字符串
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
    private const val timeZone = "Asia/Shanghai"//时间校准

    /**
     * 传入日期是否为手机当日
     *
     * @param inputDate 日期类
     * @return
     */
    @Synchronized
    @JvmStatic
    fun isToday(inputDate: Date): Boolean {
        var flag = false
        try {
            //获取当前系统时间
            val subDate = getDateTime(EN_YMD, System.currentTimeMillis())
            //定义每天的24h时间范围
            val beginTime = "$subDate 00:00:00"
            val endTime = "$subDate 23:59:59"
            //转换Date
            val dateFormat = SimpleDateFormat(EN_YMDHMS, Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone(timeZone)
            val parseBeginTime = dateFormat.parse(beginTime)
            val parseEndTime = dateFormat.parse(endTime)
            if (inputDate.after(parseBeginTime) && inputDate.before(parseEndTime)) flag = true
        } catch (ignored: ParseException) {
        }
        return flag
    }

    /**
     * 日期对比（统一年月日形式）
     *
     * @param fromSource 比较日期a
     * @param toSource   比较日期b
     * @return
     */
    @Synchronized
    @JvmStatic
    fun compareDate(fromSource: String, toSource: String): Int {
        val dateFormat = SimpleDateFormat(EN_YMD, Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone(timeZone)
        try {
            val comparedDate = dateFormat.parse(fromSource) ?: Date()
            val comparedDate2 = dateFormat.parse(toSource) ?: Date()
            return when {
                comparedDate.time > comparedDate2.time -> 1//日程时间大于系统时间
                comparedDate.time < comparedDate2.time -> -1//日程时间小于系统时间
                else -> 0
            }
        } catch (ignored: Exception) {
        }
        return 0
    }

    /**
     * 获取转换日期
     *
     * @param fromFormat 被转换的日期格式
     * @param toFormat   要转换的日期格式
     * @param source 被转换的日期
     * @return
     */
    @Synchronized
    @JvmStatic
    fun getDateFormat(fromFormat: String, toFormat: String, source: String): String {
        var result = ""
        try {
            val dateFormat = SimpleDateFormat(fromFormat, Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone(timeZone)
            result = getDateTime(toFormat, dateFormat.parse(source) ?: Date())
        } catch (ignored: ParseException) {
        }
        return result
    }

    /**
     * 传入指定日期格式的字符串转成毫秒
     *
     * @param format 日期格式
     * @param source 日期
     * @return
     */
    @Synchronized
    @JvmStatic
    fun getDateTime(format: String, source: String): Long {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone(timeZone)
        return dateFormat.parse(source)?.time ?: 0
    }


    /**
     * 传入指定日期格式和毫秒转换成字符串
     *
     * @param format 日期格式
     * @param timestamp 时间戳
     * @return
     */
    @Synchronized
    @JvmStatic
    fun getDateTime(format: String, timestamp: Long): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone(timeZone)
        return dateFormat.format(Date(timestamp)) ?: ""
    }

    /**
     * 传入指定日期格式和日期類转换成字符串
     *
     * @param format 日期格式
     * @param date 日期类
     * @return
     */
    @Synchronized
    @JvmStatic
    fun getDateTime(format: String, date: Date): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone(timeZone)
        return dateFormat.format(date) ?: ""
    }


    /**
     * 传入毫秒转换成00:00的格式
     *
     * @param timestamp 时间戳
     * @return
     */
    @Synchronized
    @JvmStatic
    fun getTime(timestamp: Long): String {
        if (timestamp <= 0) return "00:00"
        val second = (timestamp / 1000 / 60).toInt()
        val million = (timestamp / 1000 % 60).toInt()
        return "${if (second >= 10) second.toString() else "0$second"}:${if (million >= 10) million.toString() else "0$million"}"
    }

    /**
     * 获取日期的当月的第几周
     *
     * @param source 日期（yyyy-MM-dd）
     * @return
     */
    @Synchronized
    @JvmStatic
    fun getWeekOfMonth(source: String): Int {
        try {
//            val calendar = Calendar.getInstance()
//            val dateFormat = SimpleDateFormat(EN_YMD, Locale.getDefault())
//            dateFormat.timeZone = TimeZone.getTimeZone(timeZone)
//            calendar.time = dateFormat.parse(source) ?: Date()
//            return calendar.get(Calendar.WEEK_OF_MONTH)
            Calendar.getInstance().apply {
                val dateFormat = SimpleDateFormat(EN_YMD, Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone(DateUtil.timeZone)
                time = dateFormat.parse(source) ?: Date()
                return get(Calendar.WEEK_OF_MONTH)
            }
        } catch (ignored: ParseException) {
        }
        return 0
    }

    /**
     * 获取日期是第几周
     *
     * @param source 日期（yyyy-MM-dd）
     * @return
     */
    @Synchronized
    @JvmStatic
    fun getWeekOfDate(source: String): Int {
        try {
//            val calendar = Calendar.getInstance()
//            val dateFormat = SimpleDateFormat(EN_YMD, Locale.getDefault())
//            dateFormat.timeZone = TimeZone.getTimeZone(timeZone)
//            calendar.time = dateFormat.parse(source) ?: Date()
//            var weekIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
//            if (weekIndex < 0) weekIndex = 0
//            return weekIndex
            Calendar.getInstance().apply {
                val dateFormat = SimpleDateFormat(EN_YMD, Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone(DateUtil.timeZone)
                time = dateFormat.parse(source) ?: Date()
                var weekIndex = get(Calendar.DAY_OF_WEEK) - 1
                if (weekIndex < 0) weekIndex = 0
                return weekIndex
            }
        } catch (ignored: ParseException) {
        }
        return 0
    }

    /**
     * 返回中文形式的星期
     *
     * @param source 日期（yyyy-MM-dd）
     * @return
     */
    @Synchronized
    @JvmStatic
    fun getDateWeek(source: String): String {
        return when (getWeekOfDate(source)) {
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
     * 处理时间
     *
     * @param timestamp 时间戳->秒
     */
    @Synchronized
    @JvmStatic
    fun getSecondFormat(timestamp: Long): String {
        val result: String?
        val hour: Long
        val second: Long
        var minute: Long
        if (timestamp <= 0) return "00:00" else {
            minute = timestamp / 60
            if (minute < 60) {
                second = timestamp % 60
                result = "${unitFormat(minute)}:${unitFormat(second)}"
            } else {
                hour = minute / 60
                if (hour > 99) return "99:59:59"
                minute %= 60
                second = timestamp - hour * 3600 - minute * 60
                result = "${unitFormat(hour)}:${unitFormat(minute)}:${unitFormat(second)}"
            }
        }
        return result
    }

    private fun unitFormat(time: Long) = if (time in 0..9) "0$time" else "" + time

}