package com.example.klinechart.formatter

import java.util.Locale

/**
 * 对较大数据进行格式化
 */
class BigValueFormatter : IValueFormatter {
    // 必须是排好序的
    private val values = intArrayOf(10000, 1000000, 100000000)
    private val units = arrayOf("万", "百万", "亿")

    override fun format(value: Float): String {
        var mValue = value
        var unit = ""
        var i = values.size - 1
        while (i >= 0) {
            if (mValue > values[i]) {
                mValue /= values[i].toFloat()
                unit = units[i]
                break
            }
            i--
        }
        return String.format(Locale.getDefault(), "%.2f", mValue) + unit
    }

}