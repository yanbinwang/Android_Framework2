package com.example.klinechart.utils

import com.example.klinechart.entity.KLineEntity
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 数据辅助类 计算macd rsi等
 */
object DataHelper {

    /**
     * 计算RSI
     *
     * @param dataList
     */
    @JvmStatic
    fun calculateRSI(dataList: MutableList<KLineEntity>?) {
        dataList ?: return
        var rsi: Float?
        var rsiABSEma = 0f
        var rsiMaxEma = 0f
        for (i in dataList.indices) {
            val point = dataList[i]
            val closePrice = point.getClosePrice()
            if (i == 0) {
                rsi = 0f
                rsiABSEma = 0f
                rsiMaxEma = 0f
            } else {
                val rMax = 0f.coerceAtLeast(closePrice - dataList[i - 1].getClosePrice())
                val rAbs = abs(closePrice - dataList[i - 1].getClosePrice())
                rsiMaxEma = (rMax + (14f - 1) * rsiMaxEma) / 14f
                rsiABSEma = (rAbs + (14f - 1) * rsiABSEma) / 14f
                rsi = (rsiMaxEma / rsiABSEma) * 100
            }
            if (i < 13) {
                rsi = 0f
            }
            if (rsi.isNaN()) rsi = 0f
            point.mRsi = rsi
        }
    }

    /**
     * 计算kdj
     *
     * @param dataList
     */
    @JvmStatic
    fun calculateKDJ(dataList: MutableList<KLineEntity>?) {
        dataList ?: return
        var k = 0f
        var d = 0f
        for (i in dataList.indices) {
            val point = dataList[i]
            val closePrice = point.getClosePrice()
            var startIndex = i - 13
            if (startIndex < 0) {
                startIndex = 0
            }
            var max14 = Float.MIN_VALUE
            var min14 = Float.MAX_VALUE
            for (index in startIndex..i) {
                max14 = max14.coerceAtLeast(dataList.get(index).getHighPrice())
                min14 = min14.coerceAtMost(dataList.get(index).getLowPrice())
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
                point.mK = 0f
                point.mD = 0f
                point.mJ = 0f
            } else if (i == 13 || i == 14) {
                point.mK = k
                point.mD = 0f
                point.mJ = 0f
            } else {
                point.mK = k
                point.mD = d
                point.mJ = 3f * k - 2 * d
            }
        }
    }

    /**
     * 计算wr
     *
     * @param dataList
     */
    @JvmStatic
    fun calculateWR(dataList: MutableList<KLineEntity>?) {
        dataList ?: return
        var r: Float
        for (i in dataList.indices) {
            val point = dataList[i]
            var startIndex = i - 14
            if (startIndex < 0) {
                startIndex = 0
            }
            var max14 = Float.MIN_VALUE
            var min14 = Float.MAX_VALUE
            for (index in startIndex..i) {
                max14 = max14.coerceAtLeast(dataList[index].getHighPrice())
                min14 = min14.coerceAtMost(dataList[index].getLowPrice())
            }
            if (i < 13) {
                point.mR = -10f
            } else {
                r = -100 * (max14 - dataList[i].getClosePrice()) / (max14 - min14)
                if (r.isNaN()) {
                    point.mR = 0f
                } else {
                    point.mR = r
                }
            }
        }
    }

    /**
     * 计算macd
     *
     * @param dataList
     */
    @JvmStatic
    fun calculateMACD(dataList: MutableList<KLineEntity>?) {
        dataList ?: return
        var ema12 = 0f
        var ema26 = 0f
        var dif: Float
        var dea = 0f
        var macd: Float
        for (i in dataList.indices) {
            val point = dataList[i]
            val closePrice = point.getClosePrice()
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
            point.mDif = dif
            point.mDea = dea
            point.mMacd = macd
        }
    }

    /**
     * 计算 BOLL 需要在计算ma之后进行
     *
     * @param dataList
     */
    @JvmStatic
    fun calculateBOLL(dataList: MutableList<KLineEntity>?) {
        dataList ?: return
        for (i in dataList.indices) {
            val point = dataList[i]
            if (i < 19) {
                point.mMb = 0f
                point.mUp = 0f
                point.mDn = 0f
            } else {
                val n = 20
                var md = 0f
                for (j in i - n + 1..i) {
                    val c = dataList[j].getClosePrice()
                    val m = point.getMA20Price()
                    val value = c - m
                    md += value * value
                }
                md /= (n - 1)
                md = sqrt(md.toDouble()).toFloat()
                point.mMb = point.getMA20Price()
                point.mUp = point.mMb + 2f * md
                point.mDn = point.mMb - 2f * md
            }
        }
    }

    /**
     * 计算ma
     *
     * @param dataList
     */
    @JvmStatic
    fun calculateMA(dataList: MutableList<KLineEntity>?) {
        dataList ?: return
        var ma5 = 0f
        var ma10 = 0f
        var ma20 = 0f
        var ma30 = 0f
        var ma60 = 0f
        for (i in dataList.indices) {
            val point = dataList[i]
            val closePrice = point.getClosePrice()
            ma5 += closePrice
            ma10 += closePrice
            ma20 += closePrice
            ma30 += closePrice
            ma60 += closePrice
            if (i == 4) {
                point.mMA5Price = ma5 / 5f
            } else if (i >= 5) {
                ma5 -= dataList[i - 5].getClosePrice()
                point.mMA5Price = ma5 / 5f
            } else {
                point.mMA5Price = 0f
            }
            if (i == 9) {
                point.mMA10Price = ma10 / 10f
            } else if (i >= 10) {
                ma10 -= dataList[i - 10].getClosePrice()
                point.mMA10Price = ma10 / 10f
            } else {
                point.mMA10Price = 0f
            }
            if (i == 19) {
                point.mMA20Price = ma20 / 20f
            } else if (i >= 20) {
                ma20 -= dataList[i - 20].getClosePrice()
                point.mMA20Price = ma20 / 20f
            } else {
                point.mMA20Price = 0f
            }
            if (i == 29) {
                point.mMA30Price = ma30 / 30f
            } else if (i >= 30) {
                ma30 -= dataList[i - 30].getClosePrice()
                point.mMA30Price = ma30 / 30f
            } else {
                point.mMA30Price = 0f
            }
            if (i == 59) {
                point.mMA60Price = ma60 / 60f
            } else if (i >= 60) {
                ma60 -= dataList[i - 60].getClosePrice()
                point.mMA60Price = ma60 / 60f
            } else {
                point.mMA60Price = 0f
            }
        }
    }

    /**
     * 计算MA BOLL RSI KDJ MACD
     *
     * @param dataList
     */
    @JvmStatic
    fun calculate(dataList: MutableList<KLineEntity>?) {
        dataList ?: return
        calculateMA(dataList)
        calculateMACD(dataList)
        calculateBOLL(dataList)
        calculateRSI(dataList)
        calculateKDJ(dataList)
        calculateWR(dataList)
        calculateVolumeMA(dataList)
    }

    private fun calculateVolumeMA(entries: MutableList<KLineEntity>) {
        var volumeMa5 = 0f
        var volumeMa10 = 0f
        for (i in entries.indices) {
            val entry = entries[i]
            volumeMa5 += entry.getVolume()
            volumeMa10 += entry.getVolume()
            if (i == 4) {
                entry.mMA5Volume = (volumeMa5 / 5f)
            } else if (i > 4) {
                volumeMa5 -= entries[i - 5].getVolume()
                entry.mMA5Volume = volumeMa5 / 5f
            } else {
                entry.mMA5Volume = 0f
            }
            if (i == 9) {
                entry.mMA10Volume = volumeMa10 / 10f
            } else if (i > 9) {
                volumeMa10 -= entries[i - 10].getVolume()
                entry.mMA10Volume = volumeMa10 / 10f
            } else {
                entry.mMA10Volume = 0f
            }
        }
    }

}