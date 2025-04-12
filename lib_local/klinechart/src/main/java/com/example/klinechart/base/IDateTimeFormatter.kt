package com.example.klinechart.base

import java.util.Date

/**
 * 时间格式化接口
 * Created by tifezh on 2016/6/21.
 */
interface IDateTimeFormatter {
    fun format(date: Date?): String
}