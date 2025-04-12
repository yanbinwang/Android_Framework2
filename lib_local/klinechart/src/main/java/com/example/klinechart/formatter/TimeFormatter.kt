package com.example.klinechart.formatter

import com.example.klinechart.base.IDateTimeFormatter
import com.example.klinechart.utils.DateUtil
import java.util.Date

/**
 * 时间格式化器
 * Created by tifezh on 2016/6/21.
 */
class TimeFormatter : IDateTimeFormatter {
    override fun format(date: Date?): String {
        if (date == null) {
            return ""
        }
        return DateUtil.shortTimeFormat.format(date)
    }
}