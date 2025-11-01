package com.example.klinechart.formatter

import com.example.klinechart.utils.DateUtil
import java.util.Date

/**
 * 时间格式化器
 */
class TimeFormatter : IDateTimeFormatter {

    override fun format(date: Date?): String {
        if (date == null) {
            return ""
        }
        return DateUtil.shortTimeFormat.format(date)
    }

}