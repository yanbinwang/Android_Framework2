package com.github.fujianlian.klinechart

import com.example.framework.utils.function.value.min
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * 数据辅助类 计算macd rsi等
 * Created by tifezh on 2016/11/26.
 */
object DataHelper {
    /**
     * 计算MA BOLL RSI KDJ MACD
     *
     * @param dataList
     */
    fun calculate(dataList: List<KLineEntity>) {
        calculateMA(dataList)
        calculateMACD(dataList)
//        calculateBOLL(dataList)
//        calculateRSI(dataList)
//        calculateKDJ(dataList)
//        calculateWR(dataList)
//        calculateVolumeMA(dataList)
    }

    /**"recharge"
     * 计算ma
     *
     * @param dataList
     */
    fun calculateMA(dataList: List<KLineEntity>) {
        var ma5 = 0f
        var ma10 = 0f
        var ma20 = 0f
        var ma30 = 0f
        var ma60 = 0f
        for (i in dataList.indices) {
            val point = dataList[i]
            val closePrice: Float = point.closePrice
            ma5 += closePrice
            ma10 += closePrice
            ma20 += closePrice
            ma30 += closePrice
            ma60 += closePrice
            if (i == 4) {
                point.mA5Price = ma5 / 5f
            } else if (i >= 5) {
                ma5 -= dataList[i - 5].closePrice
                point.mA5Price = ma5 / 5f
            } else {
                point.mA5Price = 0f
            }
            if (i == 9) {
                point.mA10Price = ma10 / 10f
            } else if (i >= 10) {
                ma10 -= dataList[i - 10].closePrice
                point.mA10Price = ma10 / 10f
            } else {
                point.mA10Price = 0f
            }
            if (i == 19) {
                point.mA20Price = ma20 / 20f
            } else if (i >= 20) {
                ma20 -= dataList[i - 20].closePrice
                point.mA20Price = ma20 / 20f
            } else {
                point.mA20Price = 0f
            }
            if (i == 29) {
                point.mA30Price = ma30 / 30f
            } else if (i >= 30) {
                ma30 -= dataList[i - 30].closePrice
                point.mA30Price = ma30 / 30f
            } else {
                point.mA30Price = 0f
            }
            if (i == 59) {
                point.mA60Price = ma60 / 60f
            } else if (i >= 60) {
                ma60 -= dataList[i - 60].closePrice
                point.mA60Price = ma60 / 60f
            } else {
                point.mA60Price = 0f
            }
        }
    }

    /**
     * 计算macd
     *
     * @param dataList
     */
    fun calculateMACD(dataList: List<KLineEntity>) {
        var ema12 = 0f
        var ema26 = 0f
        var dif = 0f
        var dea = 0f
        var macd = 0f
        for (i in dataList.indices) {
            val point = dataList[i]
            val closePrice: Float = point.closePrice
            if (i == 0) {
                ema12 = closePrice
                ema26 = closePrice
            } else {
                // EMA（12） = 前一日EMA（12） X 11/13 + 今日收盘价 X 2/13
                ema12 = ema12 * 11f / 13f + closePrice * 2f / 13f
                // EMA（26） = 前一日EMA（26） X 25/27 + 今日收盘价 X 2/27
                ema26 = ema26 * 25f / 27f + closePrice * 2f / 27f
            }
            // DIF = EMA（12） - EMA（26） 。
            // 今日DEA = （前一日DEA X 8/10 + 今日DIF X 2/10）
            // 用（DIF-DEA）*2即为MACD柱状图。
            dif = ema12 - ema26
            dea = dea * 8f / 10f + dif * 2f / 10f
            macd = (dif - dea) * 2f
            point.dif = dif
            point.dea = dea
            point.macd = macd
        }
    }

    /**
     * 计算 BOLL 需要在计算ma之后进行
     *
     * @param dataList
     */
    fun calculateBOLL(dataList: List<KLineEntity>) {
        for (i in dataList.indices) {
            val point = dataList[i]
            if (i < 19) {
                point.mb = 0f
                point.up = 0f
                point.dn = 0f
            } else {
                val n = 20
                var md = 0f
                for (j in i - n + 1..i) {
                    val c = dataList[j].closePrice
                    val m = point.closePrice
                    val value = c - m
                    md += value * value
                }
                md /= (n - 1)
                md = sqrt(md.toDouble()).toFloat()
                point.mb = point.mA20Price
                point.up = point.mb + 2f * md
                point.dn = point.mb - 2f * md
            }
        }
    }

    /**
     * 计算RSI
     *
     * @param dataList
     */
    fun calculateRSI(dataList: List<KLineEntity>) {
        var rsi: Float
        var rsiABSEma = 0f
        var rsiMaxEma = 0f
        for (i in dataList.indices) {
            val point = dataList[i]
            val closePrice: Float = point.closePrice
            if (i == 0) {
                rsi = 0f
                rsiABSEma = 0f
                rsiMaxEma = 0f
            } else {
                val Rmax = max(0f, closePrice - dataList[i - 1].closePrice)
                val RAbs: Float = abs(closePrice - dataList[i - 1].closePrice)
                rsiMaxEma = (Rmax + (14f - 1) * rsiMaxEma) / 14f
                rsiABSEma = (RAbs + (14f - 1) * rsiABSEma) / 14f
                rsi = rsiMaxEma / rsiABSEma * 100
            }
            if (i < 13) {
                rsi = 0f
            }
            if (rsi.isNaN()) rsi = 0f
            point.rsi = rsi
        }
    }

    /**
     * 计算kdj
     *
     * @param dataList
     */
    fun calculateKDJ(dataList: List<KLineEntity>) {
        var k = 0f
        var d = 0f
        for (i in dataList.indices) {
            val point = dataList[i]
            val closePrice: Float = point.closePrice
            var startIndex = (i - 13).min(0)
            var max14 = Float.MIN_VALUE
            var min14 = Float.MAX_VALUE
            for (index in startIndex..i) {
                max14 = max(max14, dataList[index].highPrice)
                min14 = min(min14, dataList[index].lowPrice)
            }
            var rsv = 100f * (closePrice - min14) / (max14 - min14)
            if (rsv.isNaN()) {
                rsv = 0f
            }
            if (i == 0) {
                k = 50f
                d = 50f
            } else {
                k = (rsv + 2f * k) / 3f
                d = (k + 2f * d) / 3f
            }
            if (i < 13) {
                point.k = 0f
                point.d = 0f
                point.j = 0f
            } else if (i == 13 || i == 14) {
                point.k = k
                point.d = 0f
                point.j = 0f
            } else {
                point.k = k
                point.d = d
                point.j = 3f * k - 2 * d
            }
        }
    }

    /**
     * 计算wr
     *
     * @param dataList
     */
    fun calculateWR(dataList: List<KLineEntity>) {
        var r: Float
        for (i in dataList.indices) {
            val point = dataList[i]
            var startIndex = (i - 14).min(0)
            var max14 = Float.MIN_VALUE
            var min14 = Float.MAX_VALUE
            for (index in startIndex..i) {
                max14 = max(max14, dataList[index].highPrice)
                min14 = min(min14, dataList[index].lowPrice)
            }
            if (i < 13) {
                point.r = -10f
            } else {
                r = -100 * (max14 - dataList[i].closePrice) / (max14 - min14)
                if (r.isNaN()) {
                    point.r = 0f
                } else {
                    point.r = r
                }
            }
        }
    }

    private fun calculateVolumeMA(entries: List<KLineEntity>) {
        var volumeMa5 = 0f
        var volumeMa10 = 0f
        for (i in entries.indices) {
            val entry = entries[i]
            volumeMa5 += entry.volume
            volumeMa10 += entry.volume
            if (i == 4) {
                entry.mA5Volume = volumeMa5 / 5f
            } else if (i > 4) {
                volumeMa5 -= entries[i - 5].volume
                entry.mA5Volume = volumeMa5 / 5f
            } else {
                entry.mA5Volume = 0f
            }
            if (i == 9) {
                entry.mA10Volume = volumeMa10 / 10f
            } else if (i > 9) {
                volumeMa10 -= entries[i - 10].volume
                entry.mA10Volume = volumeMa10 / 10f
            } else {
                entry.mA10Volume = 0f
            }
        }
    }
}