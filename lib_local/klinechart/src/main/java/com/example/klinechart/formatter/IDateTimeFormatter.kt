package com.example.klinechart.formatter

import java.util.Date

/**
 * 时间格式化接口
 */
interface IDateTimeFormatter {

    fun format(date: Date?): String

}