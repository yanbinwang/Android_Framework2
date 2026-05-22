package com.example.klinechart.utils.formatter

import java.util.Locale

/**
 * Value格式化类
 */
class ValueFormatter: IValueFormatter {

    override fun format(value: Float): String {
        // K 线图、指标数值必须用英语格式，小数点才不会乱
        return String.format(Locale.ROOT, "%.2f", value)
    }

}