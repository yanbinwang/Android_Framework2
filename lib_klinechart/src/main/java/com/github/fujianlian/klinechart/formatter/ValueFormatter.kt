package com.github.fujianlian.klinechart.formatter

import com.github.fujianlian.klinechart.base.IValueFormatter

/**
 * Value格式化类
 * Created by tifezh on 2016/6/21.
 */
class ValueFormatter : IValueFormatter {
    override fun format(value: Float): String {
        return String.format("%.2f", value)
    }
}