package com.example.klinechart.formatter

/**
 * Value格式化接口
 */
interface IValueFormatter {

    /**
     * 格式化value
     *
     * @param value 传入的value值
     * @return 返回字符串
     */
    fun format(value: Float): String

}