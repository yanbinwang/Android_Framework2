package com.example.klinechart.utils.formatter.value

import java.util.Locale

/**
 * 大数单位换算：万 / 百万 / 亿
 */
class UnitValueFormatter : IValueFormatter {
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
        return String.format(Locale.ROOT, "%.2f", mValue) + unit
    }

}