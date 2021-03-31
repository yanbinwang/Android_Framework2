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
    const val EN_M_FORMAT = "MM"
    const val EN_MD_FORMAT = "MM-dd"
    const val EN_HM_FORMAT = "HH:mm"
    const val EN_HMS_FORMAT = "HH:mm:ss"
    const val EN_YM_FORMAT = "yyyy-MM"
    const val EN_YMD_FORMAT = "yyyy-MM-dd"
    const val EN_YMDHM_FORMAT = "yyyy-MM-dd HH:mm"
    const val EN_YMDHMS_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val CN_M_FORMAT = "M月"
    const val CN_MD_FORMAT = "M月d日"
    const val CN_HM_FORMAT = "HH时mm分"
    const val CN_YM_FORMAT = "yyyy年M月"
    const val CN_YMD_FORMAT = "yyyy年MM月dd日"
    const val CN_YMDHM_FORMAT = "yyyy年MM月dd日 HH时mm分"
    const val CN_YMDHMS_FORMAT = "yyyy年MM月dd日 HH时mm分ss秒"

    /**
     * 获取转换日期
     *
     * @param fromFormat 被转换的日期格式
     * @param toFormat   要转换的日期格式
     * @param dateFormat 本转换的日期
     * @return
     */
    @JvmStatic
    fun getDateFormat(fromFormat: String, toFormat: String, dateFormat: String): String {
        var result = ""
        if (!TextUtils.isEmpty(dateFormat)) {
            try {
                //传入格式转换成日期
                val date = SimpleDateFormat(fromFormat, Locale.getDefault()).parse(dateFormat)
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
     * @param format
     * @param dateFormat
     * @return
     */
    @JvmStatic
    fun getDateTime(format: String, dateFormat: String): Long {
        try {
            return SimpleDateFormat(format, Locale.getDefault()).parse(dateFormat).time
        } catch (e: ParseException) {
        }
        return 0
    }

    /**
     * 传入指定日期格式和毫秒转换成字符串
     * @param format
     * @param timestamp
     * @return
     */
    @JvmStatic
    fun getDateTimeStr(format: String, timestamp: Long): String {
        return SimpleDateFormat(format, Locale.getDefault()).format(Date(timestamp))
    }

    /**
     * 传入指定日期格式和日期類转换成字符串
     * @param format
     * @param timestamp
     * @return
     */
    @JvmStatic
    fun getDateTimeStr(format: String, date: Date): String {
        return SimpleDateFormat(format, Locale.getDefault()).format(date)
    }

    /**
     * 传入毫秒转换成00:00的格式
     * @param time
     * @return
     */
    @JvmStatic
    fun getTimeStr(time: Long): String {
        if (time <= 0) {
            return "00:00"
        }
        val second = (time / 1000 / 60).toInt()
        val million = (time / 1000 % 60).toInt()
        val f = if (second >= 10) second.toString() else "0$second"
        val m = if (million >= 10) million.toString() else "0$million"
        return "$f:$m"
    }

    /**
     * 日期对比（统一年月日形式）
     *
     * @param fromDate 被比较日期
     * @param toDate   比较日期
     * @return
     */
    @JvmStatic
    fun compareDate(fromDate: String, toDate: String): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val comparedDate = dateFormat.parse(fromDate)
            val comparedDate2 = dateFormat.parse(toDate)
            return when {
                comparedDate.time > comparedDate2.time -> 1//日程时间大于系统时间
                comparedDate.time < comparedDate2.time -> -1//日程时间小于系统时间
                else -> 0
            }
        } catch (e: Exception) {
        }
        return 0
    }

    /**
     * 传入日期是否为手机当日
     *
     * @param inputDate
     * @return
     */
    @JvmStatic
    fun isToday(inputDate: Date): Boolean {
        var flag = false
        try {
            // 获取当前系统时间
            val longDate = System.currentTimeMillis()
            val nowDate = Date(longDate)
            val dateFormat = SimpleDateFormat(EN_YMDHMS_FORMAT, Locale.getDefault())
            val format = dateFormat.format(nowDate)
            val subDate = format.substring(0, 10)
            // 定义每天的24h时间范围
            val beginTime = "$subDate 00:00:00"
            val endTime = "$subDate 23:59:59"
            val parseBeginTime = dateFormat.parse(beginTime)
            val parseEndTime = dateFormat.parse(endTime)
            if (inputDate.after(parseBeginTime) && inputDate.before(parseEndTime)) {
                flag = true
            }
        } catch (e: ParseException) {
        }
        return flag
    }

    /**
     * 获取日期的当月的第几周
     *
     * @param inputDate
     * @return
     */
    @JvmStatic
    fun getWeekOfMonth(inputDate: String): Int {
        try {
            val simpleDateFormat = SimpleDateFormat(EN_YMD_FORMAT, Locale.getDefault())
            val time = simpleDateFormat.parse(inputDate)
            val calendar = Calendar.getInstance()
            calendar.time = time
            return calendar.get(Calendar.WEEK_OF_MONTH)
        } catch (e: ParseException) {
        }
        return 0
    }

    /**
     * 获取日期是第几周
     *
     * @param inputDate
     * @return
     */
    @JvmStatic
    fun getWeekOfDate(inputDate: String): Int {
        try {
            val simpleDateFormat = SimpleDateFormat(EN_YMD_FORMAT, Locale.getDefault())
            val time = simpleDateFormat.parse(inputDate)
            val calendar = Calendar.getInstance()
            calendar.time = time
            var weekIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
            if (weekIndex < 0) {
                weekIndex = 0
            }
            return weekIndex
        } catch (e: ParseException) {
        }
        return 0
    }

    /**
     * 返回中文形式的星期
     *
     * @param inputDate
     * @return
     */
    @JvmStatic
    fun getDateWeekStr(inputDate: String): String {
        return when (getWeekOfDate(inputDate)) {
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