package com.example.klinechart.utils.formatter.date

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 时间格式化器
 */
class ShortTimeFormatter : IDateTimeFormatter {
    private val shortTimeFormat by lazy { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    override fun format(date: Date?): String {
        return if (date != null) {
            shortTimeFormat.format(date)
        } else {
            ""
        }
    }

}