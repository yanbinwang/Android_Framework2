package com.example.klinechart.formatter

/**
 * Value格式化类
 */
class ValueFormatter: IValueFormatter {

    override fun format(value: Float): String {
        return String.format("%.2f", value)
    }

}