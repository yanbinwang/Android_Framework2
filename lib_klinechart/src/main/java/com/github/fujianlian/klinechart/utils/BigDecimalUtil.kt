package com.github.fujianlian.klinechart.utils

import java.math.BigDecimal

/**
 * 小数精确工具类
 */
object BigDecimalUtil {
    /**
     * @param num    要修改的数字
     * @param digits 保留几位小数点
     * @param mode   保留模式
     * @return 返回小数
     */
    fun getBigDecimal(num: Double, digits: Int, mode: Int): String {
        val bigDecimal = BigDecimal.valueOf(num)
        return bigDecimal.setScale(digits, mode).toString()
    }
}