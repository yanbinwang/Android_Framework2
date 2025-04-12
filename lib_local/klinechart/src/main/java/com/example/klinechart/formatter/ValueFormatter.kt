package com.example.klinechart.formatter

import android.annotation.SuppressLint
import com.example.klinechart.base.IValueFormatter

/**
 * Value格式化类
 * Created by tifezh on 2016/6/21.
 */
@SuppressLint("DefaultLocale")
class ValueFormatter : IValueFormatter {
    override fun format(value: Float): String {
        return String.format("%.2f", value)
    }
}