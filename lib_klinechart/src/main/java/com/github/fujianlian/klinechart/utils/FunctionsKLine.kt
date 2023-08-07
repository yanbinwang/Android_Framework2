package com.github.fujianlian.klinechart.utils

import com.github.fujianlian.klinechart.KLineChartView
import com.github.fujianlian.klinechart.base.IAdapter
import com.github.fujianlian.klinechart.draw.Status
import com.github.fujianlian.klinechart.formatter.DateFormatter

/**
 * 设置k线图基础布局
 */
fun <T : IAdapter> KLineChartView?.init(klineAdapter: T?, rows: Int = 4, columns: Int = 4) {
    if (this == null || klineAdapter == null) return
    adapter = klineAdapter
    dateTimeFormatter = DateFormatter()
    setGridRows(rows)
    setGridColumns(columns)
    justShowLoading()
}

/**
 * status：MA, BOLL, NONE
 */
fun KLineChartView?.mainDrawType(status: Status) {
    if (this == null) return
    hideSelectData()
    changeMainDrawType(status)
}

/**
 * 是否是分时图
 */
fun KLineChartView?.mainDrawLine(isLine: Boolean) {
    if (this == null) return
    hideSelectData()
    setMainDrawLine(isLine)
}

/**
 * 展示对应数据（subIndex下标对应）：macd,kdj,rsi,wr
 */
fun KLineChartView?.showChild(subIndex: Int) {
    if (this == null) return
    hideSelectData()
    setChildDraw(subIndex)
}

/**
 * 隐藏所有子视图
 */
fun KLineChartView?.hideChild() {
    if (this == null) return
    hideSelectData()
    hideChildDraw()
}