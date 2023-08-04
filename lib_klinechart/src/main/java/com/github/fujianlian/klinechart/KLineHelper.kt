package com.github.fujianlian.klinechart

/**
 * description
 * creator Hyatt
 */
object KLineHelper {
    const val RISE_RED = "RISE_RED"
    const val RISE_GREEN = "RISE_GREEN"
    const val IS_RISE_RED = "IS_RISE_RED"
    //    @get:Keep private val isRiseRed by CacheString(IS_RISE_RED, "0")
    private val isRiseRed = "0"

    var chartHigh: String = ""
    var chartLow: String = ""
    var chartOpen: String = ""
    var chartClose: String = ""

    fun setText(h: String, l: String, o: String, c: String) {
        chartHigh = h
        chartLow = l
        chartOpen = o
        chartClose = c
    }

    fun isRiseRed(): Boolean {
        return when (isRiseRed) {
            RISE_RED -> true
            RISE_GREEN -> false
            else -> false
        }
    }
}