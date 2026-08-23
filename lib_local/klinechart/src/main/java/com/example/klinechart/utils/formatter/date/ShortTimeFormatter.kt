package com.example.klinechart.utils.formatter.date

import com.example.framework.utils.function.value.convert
import java.util.Date

/**
 * 时间格式化器
 */
class ShortTimeFormatter : IDateTimeFormatter {
//    private val shortTimeFormat by lazy { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    override fun format(date: Date?): String {
        return if (date != null) {
//            shortTimeFormat.format(date)
            "HH:mm".convert(date)
        } else {
            ""
        }
    }

}