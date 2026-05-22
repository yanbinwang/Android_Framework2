package com.example.klinechart.utils.formatter

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 时间格式化器
 */
class DateFormatter : IDateTimeFormatter {
    private val dateFormat by lazy { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    override fun format(date: Date?): String {
        return if (date != null) {
            dateFormat.format(date)
        } else {
            ""
        }
    }

}