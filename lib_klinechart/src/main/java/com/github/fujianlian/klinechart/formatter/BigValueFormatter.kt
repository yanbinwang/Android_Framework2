package com.github.fujianlian.klinechart.formatter

import com.github.fujianlian.klinechart.base.IValueFormatter
import com.github.fujianlian.klinechart.utils.BigDecimalUtil
import java.math.BigDecimal

/**
 * 对较大数据进行格式化
 * Created by tifezh on 2017/12/13.
 */
class BigValueFormatter : IValueFormatter {
    //必须是排好序的
    private val values = intArrayOf(1000, 1000000, 1000000000)
    private val units = arrayOf("K", "M", "B")

    override fun format(value: Float, digits: String): String {
        var newValue = value
        var unit = ""
        var i = values.size - 1
        while (i >= 0) {
            if (newValue > values[i]) {
                newValue /= values[i]
                unit = units[i]
                break
            }
            i--
        }
//        return BigDecimalUtil.getBigDecimal(newValue.toDouble(), digits.toInt(), BigDecimal.ROUND_HALF_UP) + unit
        return BigDecimalUtil.getBigDecimal(newValue.toDouble(), 0, BigDecimal.ROUND_HALF_UP) + unit
    }

}