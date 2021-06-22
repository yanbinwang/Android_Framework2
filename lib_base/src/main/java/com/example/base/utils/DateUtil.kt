package com.example.base.utils

import android.text.TextUtils
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
            val longDate = System.currentTimeMillis()
            val nowDate = Date(longDate)
            val dateFormat = SimpleDateFormat(EN_YMDHMS, Locale.getDefault())
            val format = dateFormat.format(nowDate)
            val subDate = format.substring(0, 10)
            //定义每天的24h时间范围
            val beginTime = "$subDate 00:00:00"
            val endTime = "$subDate 23:59:59"
            val parseBeginTime = dateFormat.parse(beginTime)
            val parseEndTime = dateFormat.parse(endTime)
            if (inputDate.after(parseBeginTime) && inputDate.before(parseEndTime)) flag = true
        } catch (e: ParseException) {
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
        try {
            val comparedDate = dateFormat.parse(fromSource)
            val comparedDate2 = dateFormat.parse(toSource)
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
        if (!TextUtils.isEmpty(source)) {
            try {
                //传入格式转换成日期
                val date = SimpleDateFormat(fromFormat, Locale.getDefault()).parse(source)
                //日期转换成想要的格式
                result = SimpleDateFormat(toFormat, Locale.getDefault()).format(date)
            } catch (e: ParseException) {
            }
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
    fun getDateTime(format: String, source: String) = SimpleDateFormat(format, Locale.getDefault()).parse(source)?.time ?: 0

    /**
     * 传入指定日期格式和毫秒转换成字符串
     *
     * @param format 日期格式
     * @param timestamp 时间戳
     * @return
     */
    @Synchronized
    @JvmStatic
    fun getDateTime(format: String, timestamp: Long) = SimpleDateFormat(format, Locale.getDefault()).format(Date(timestamp))

    /**
     * 传入指定日期格式和日期類转换成字符串
     *
     * @param format 日期格式
     * @param date 日期类
     * @return
     */
    @Synchronized
    @JvmStatic
    fun getDateTime(format: String, date: Date) = SimpleDateFormat(format, Locale.getDefault()).format(date)

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
        val f = if (second >= 10) second.toString() else "0$second"
        val m = if (million >= 10) million.toString() else "0$million"
        return "$f:$m"
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
            val calendar = Calendar.getInstance()
            calendar.time = SimpleDateFormat(EN_YMD, Locale.getDefault()).parse(source)
            return calendar.get(Calendar.WEEK_OF_MONTH)
        } catch (e: ParseException) {
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
            val calendar = Calendar.getInstance()
            calendar.time = SimpleDateFormat(EN_YMD, Locale.getDefault()).parse(source)
            var weekIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
            if (weekIndex < 0) weekIndex = 0
            return weekIndex
        } catch (e: ParseException) {
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

}