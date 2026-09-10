package com.example.klinechart.utils.formatter.date

import java.util.Date

/**
 * 时间格式化接口
 */
interface ITimeFormatter {

    fun format(date: Date?): String

}