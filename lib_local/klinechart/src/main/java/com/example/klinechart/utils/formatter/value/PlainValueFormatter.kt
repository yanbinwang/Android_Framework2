package com.example.klinechart.utils.formatter.value

import java.util.Locale

/**
 * 普通数值保留两位小数
 */
class PlainValueFormatter: IValueFormatter {

    override fun format(value: Float): String {
        // K 线图、指标数值必须用英语格式，小数点才不会乱
        return String.format(Locale.ROOT, "%.2f", value)
    }

}