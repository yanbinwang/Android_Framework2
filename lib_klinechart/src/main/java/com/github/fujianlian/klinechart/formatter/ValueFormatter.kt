package com.github.fujianlian.klinechart.formatter

import com.example.framework.utils.function.value.toSafeDouble
import com.example.framework.utils.function.value.toSafeInt
import com.github.fujianlian.klinechart.base.IValueFormatter
import com.github.fujianlian.klinechart.utils.BigDecimalUtil
import java.math.BigDecimal

/**
 * Value格式化类
 * Created by tifezh on 2016/6/21.
 */
class ValueFormatter : IValueFormatter {
    override fun format(value: Float, digits: String): String {
        return BigDecimalUtil.getBigDecimal(value.toSafeDouble(), digits.toSafeInt(2), BigDecimal.ROUND_HALF_UP)
    }
}