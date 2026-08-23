package com.example.klinechart.utils.formatter.date

import com.example.framework.utils.function.value.convert
import java.util.Date

/**
 * 时间格式化器
 */
class DateFormatter : IDateTimeFormatter {
//    private val dateFormat by lazy { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    override fun format(date: Date?): String {
        return if (date != null) {
//            dateFormat.format(date)
            "yyyy/MM/dd".convert(date)
        } else {
            ""
        }
    }

}