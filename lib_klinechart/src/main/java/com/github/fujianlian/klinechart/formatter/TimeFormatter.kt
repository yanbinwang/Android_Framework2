package com.github.fujianlian.klinechart.formatter

import com.github.fujianlian.klinechart.base.IDateTimeFormatter
import com.github.fujianlian.klinechart.utils.DateUtil
import java.util.Date

/**
 * 时间格式化器
 * Created by tifezh on 2016/6/21.
 */
class TimeFormatter : IDateTimeFormatter {
    override fun format(date: Date?): String {
        return if (date == null) "" else DateUtil.shortTimeFormat.format(date)
    }
}