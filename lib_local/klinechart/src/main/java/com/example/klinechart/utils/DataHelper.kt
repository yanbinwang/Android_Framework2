package com.example.klinechart.utils

import com.example.klinechart.bean.KLineChartBean
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 数据辅助类 计算 MACD/RSI 等
 */
object DataHelper {

    /**
     * 计算 RSI 指标 (最近14天里，涨的幅度占总波动幅度的百分比)
     */
    fun calculateRSI(data: MutableList<KLineChartBean>?) {
        data ?: return
        // 用来存当前这根 K 线算出来的 RSI 值
        var rsi: Float?
        // 累计"总波动幅度"的平滑值（初始为0）-> 不管涨跌总共动了多少
        var rsiABSEma = 0f
        // 累计"上涨幅度"的平滑值（初始为0）-> 涨了多少
        var rsiMaxEma = 0f
        // 开始循环每一根 K 线
        for (i in data.indices) {
            // 拿到当前这根K线的数据
            val point = data[i]
            // 拿到当前收盘价
            val closePrice = point.getClosePrice()
            // 如果是第一根K线（没有"前一天"可以对比）
            if (i == 0) {
                // RSI直接设为0，两个EMA也归零
                rsi = 0f
                rsiABSEma = 0f
                rsiMaxEma = 0f
            } else {
                // 算"今天比昨天涨了多少"，如果跌了就算0（只取正数）
                val rMax = 0f.coerceAtLeast(closePrice - data[i - 1].getClosePrice())
                // 算"今天比昨天总共动了多少"（取绝对值，涨跌都算）
                val rAbs = abs(closePrice - data[i - 1].getClosePrice())
                // 用 EMA 公式更新"上涨幅度"的平滑值，等价于：新值 = (今天的涨幅 + 13 × 昨天的旧值) / 14
                rsiMaxEma = (rMax + (14f - 1) * rsiMaxEma) / 14f
                // 用 EMA 公式更新"总波动幅度"的平滑值
                rsiABSEma = (rAbs + (14f - 1) * rsiABSEma) / 14f
                // RSI = 上涨占比 × 100
                rsi = (rsiMaxEma / rsiABSEma) * 100
            }
            // 前13根K线（索引0~12），数据还不够14根，强制设为0，不显示
            if (i < 13) {
                rsi = 0f
            }
            // 如果除零了（总波动为0），结果会是NaN，改成0防崩 -> 当连续几天价格完全不动时，rsiABSEma 可能是0，除以0会得到 NaN
            if (rsi.isNaN()) rsi = 0f
            // 把算好的RSI存回这根K线的对象里
            point.mRsi = rsi
        }
    }

    /**
     * 计算 KDJ (随机指标)
     * 公式：RSV = (收盘价 - 14日最低价) / (14日最高价 - 14日最低价) × 100
     *       K = (RSV + 2×昨日K) / 3    （初始值50）
     *       D = (K + 2×昨日D) / 3      （初始值50）
     *       J = 3K - 2D
     */
    fun calculateKDJ(data: MutableList<KLineChartBean>?) {
        data ?: return
        // K、D 的递推变量，初始值在 i==0 时设为 50
        var k = 0f
        var d = 0f
        for (i in data.indices) {
            val point = data[i]
            val closePrice = point.getClosePrice()
            // ========== 第一步：找最近14根K线的最高价和最低价 ==========
            // 窗口起点：往前数13根（含当前这根共14根），不够就从0开始
            var startIndex = i - 13
            if (startIndex < 0) {
                startIndex = 0
            }
            // 14日内最高价/最低价
            var max14 = Float.MIN_VALUE
            var min14 = Float.MAX_VALUE
            // 遍历窗口内所有K线，取极值
            for (index in startIndex..i) {
                max14 = max14.coerceAtLeast(data[index].getHighPrice())
                min14 = min14.coerceAtMost(data[index].getLowPrice())
            }
            // ========== 第二步：计算 RSV（未成熟随机值）==========
            // RSV 表示当前收盘价在14日波动范围中的位置（0~100）
            var rsv = 100f * (closePrice - min14) / (max14 - min14)
            // 当14天内最高价==最低价时，除数为0，结果为NaN，兜底为0
            if (rsv.isNaN()) {
                rsv = 0f
            }
            // ========== 第三步：用 SMA 平滑算 K、D ==========
            if (i == 0) {
                // 第一根没有"昨日K/D"，按惯例初始化为50
                k = 50f
                d = 50f
            } else {
                // K = (今日RSV + 2×昨日K) / 3  → 等价于 1/3 RSV + 2/3 昨日K
                k = (rsv + 2f * k) / 3f
                // D = (今日K + 2×昨日D) / 3   → 对K再做一次平滑
                d = (k + 2f * d) / 3f
            }
            // ========== 第四步：按数据充足程度分段赋值 ==========
            if (i < 13) {
                // 前13根：数据不足14天，KD全部置0不显示
                point.mK = 0f
                point.mD = 0f
                point.mJ = 0f
            } else if (i == 13 || i == 14) {
                // 第14、15根：K刚凑够有效数据，但D还需要再平滑一轮才可靠，所以只输出K，D和J暂时置0
                point.mK = k
                point.mD = 0f
                point.mJ = 0f
            } else {
                // 第16根起：K、D都已充分平滑，正常输出三条线
                point.mK = k
                point.mD = d
                point.mJ = 3f * k - 2f * d
            }
        }
    }

    /**
     * 计算 WR（威廉指标 / Williams %R）
     * 公式：WR = -100 × (14日最高价 - 收盘价) / (14日最高价 - 14日最低价)
     * 值域：-100 ~ 0
     *     -80 ~ -100 → 超卖区（可能反弹）
     *     -20 ~ 0   → 超买区（可能回调）
     */
    fun calculateWR(data: MutableList<KLineChartBean>?) {
        data ?: return
        var r: Float
        for (i in data.indices) {
            val point = data[i]
            // ========== 第一步：找最近15根K线的最高价和最低价 ==========
            // 注意：这里窗口是 i-14，含当前共15根；而KDJ用的是 i-13（14根）
            // 请确认后端WR确实使用15日周期，而非14日
            var startIndex = i - 14
            if (startIndex < 0) {
                startIndex = 0
            }
            // 窗口内最高价/最低价
            var max14 = Float.MIN_VALUE
            var min14 = Float.MAX_VALUE
            for (index in startIndex..i) {
                max14 = max14.coerceAtLeast(data[index].getHighPrice())
                min14 = min14.coerceAtMost(data[index].getLowPrice())
            }
            // ========== 第二步：按数据充足程度赋值 ==========
            if (i < 13) {
                // 前13根：数据不足，置为无效值 -10f（不显示或特殊处理）
                point.mWr = -10f
            } else {
                // WR = -100 × (最高价 - 收盘价) / (最高价 - 最低价)
                // 分子用"最高价-收盘价"而非"收盘价-最低价"，所以结果是负数
                r = -100f * (max14 - data[i].getClosePrice()) / (max14 - min14)
                // 当最高价==最低价时除数为0，结果为NaN，兜底为0
                if (r.isNaN()) {
                    point.mWr = 0f
                } else {
                    point.mWr = r
                }
            }
        }
    }

    /**
     * 计算 MACD（指数平滑异同移动平均线）
     * 公式：
     *   EMA12 = 前一日EMA12 × 11/13 + 今日收盘价 × 2/13
     *   EMA26 = 前一日EMA26 × 25/27 + 今日收盘价 × 2/27
     *   DIF   = EMA12 - EMA26
     *   DEA   = 前一日DEA × 8/10 + 今日DIF × 2/10
     *   MACD柱 = (DIF - DEA) × 2
     */
    fun calculateMACD(data: MutableList<KLineChartBean>?) {
        data ?: return
        var ema12 = 0f
        var ema26 = 0f
        var dif: Float
        var dea = 0f
        var macd: Float
        for (i in data.indices) {
            val point = data[i]
            val closePrice = point.getClosePrice()
            // ========== 第一步：计算两条 EMA ==========
            if (i == 0) {
                // 第一根K线：EMA 初始值 = 当日收盘价
                ema12 = closePrice
                ema26 = closePrice
            } else {
                // EMA(N) = 前一日EMA × (N-1)/(N+1) + 今日收盘价 × 2/(N+1)
                // EMA（12） = 前一日EMA（12） X 11/13 + 今日收盘价 X 2/13
                ema12 = ema12 * 11f / 13f + closePrice * 2f / 13f
                // EMA（26） = 前一日EMA（26） X 25/27 + 今日收盘价 X 2/27
                ema26 = ema26 * 25f / 27f + closePrice * 2f / 27f
            }
            // ========== 第二步：计算 DIF、DEA、MACD柱 ==========
            // DIF = EMA12 - EMA26
            dif = ema12 - ema26
            // DEA = DIF 的 EMA(9) = 前一日DEA × 8/10 + 今日DIF × 2/10
            // 首根 dea=0，等价于 dif×2/10；后端采用从0递推，保持一致即可
            dea = dea * 8f / 10f + dif * 2f / 10f
            // MACD柱状图 = (DIF - DEA) × 2，放大差值便于观察
            macd = (dif - dea) * 2f
            // ========== 第三步：赋值 ==========
            point.mDif = dif
            point.mDea = dea
            point.mMacd = macd
        }
    }

    /**
     * 计算 BOLL（布林带）
     * 必须在 calculateMA() 之后调用，依赖 MA20 数据
     * 公式（周期20，倍数2）：
     *   中轨 MB = MA20
     *   标准差 MD = √(Σ(Close - MB)² / N)    ← 注意确认后端用的是 N 还是 N-1
     *   上轨 UP = MB + 2 × MD
     *   下轨 DN = MB - 2 × MD
     * 值域：前19根数据不足，置为0
     */
    fun calculateBOLL(data: MutableList<KLineChartBean>?) {
        data ?: return
        val n = 20
        for (i in data.indices) {
            val point = data[i]
            if (i < 19) {
                // 前19根：MA20尚未形成，布林带无效
                point.mMb = 0f
                point.mUp = 0f
                point.mDn = 0f
            } else {
                // MA20 提到外层，避免内层循环重复调用20次
                val m = point.getMA20Price()
                var md = 0f
                for (j in i - n + 1..i) {
                    val c = data[j].getClosePrice()
                    val value = c - m
                    md += value * value
                }
                // 保持原有 n-1（样本标准差）不变
                md /= (n - 1)
                md = sqrt(md.toDouble()).toFloat()
                point.mMb = m
                point.mUp = point.mMb + 2f * md
                point.mDn = point.mMb - 2f * md
            }
        }
    }

    /**
     * 计算 MA 均线（MA5/MA10/MA20/MA30/MA60）
     * 采用滑动窗口累加法，避免每根K线重复循环求和
     */
    fun calculateMA(data: MutableList<KLineChartBean>?) {
        data ?: return
        // 5条均线的滑动累加和
        var ma5 = 0f
        var ma10 = 0f
        var ma20 = 0f
        var ma30 = 0f
        var ma60 = 0f
        for (i in data.indices) {
            val point = data[i]
            val closePrice = point.getClosePrice()
            // 将当前收盘价累加到所有均线的和中
            ma5 += closePrice
            ma10 += closePrice
            ma20 += closePrice
            ma30 += closePrice
            ma60 += closePrice
            // ---------- MA5 ----------
            if (i == 4) {
                // 第5根K线（索引4）：首次凑齐5个数据，计算初始MA5
                point.mMA5Price = ma5 / 5f
            } else if (i >= 5) {
                // 后续K线：滑出最旧的一个收盘价，保持窗口大小为5
                ma5 -= data[i - 5].getClosePrice()
                point.mMA5Price = ma5 / 5f
            } else {
                // 前4根K线：数据不足，MA5无效
                point.mMA5Price = 0f
            }
            // ---------- MA10 ----------
            if (i == 9) {
                // 第10根K线（索引9）：首次凑齐10个数据，计算初始MA10
                point.mMA10Price = ma10 / 10f
            } else if (i >= 10) {
                // 后续K线：滑出最旧的一个收盘价，保持窗口大小为10
                ma10 -= data[i - 10].getClosePrice()
                point.mMA10Price = ma10 / 10f
            } else {
                // 前9根K线：数据不足，MA10无效
                point.mMA10Price = 0f
            }
            // ---------- MA20 ----------
            if (i == 19) {
                // 第20根K线（索引19）：首次凑齐20个数据，计算初始MA20
                point.mMA20Price = ma20 / 20f
            } else if (i >= 20) {
                // 后续K线：滑出最旧的一个收盘价，保持窗口大小为20
                ma20 -= data[i - 20].getClosePrice()
                point.mMA20Price = ma20 / 20f
            } else {
                // 前19根K线：数据不足，MA20无效
                point.mMA20Price = 0f
            }
            // ---------- MA30 ----------
            if (i == 29) {
                // 第30根K线（索引29）：首次凑齐30个数据，计算初始MA30
                point.mMA30Price = ma30 / 30f
            } else if (i >= 30) {
                // 后续K线：滑出最旧的一个收盘价，保持窗口大小为30
                ma30 -= data[i - 30].getClosePrice()
                point.mMA30Price = ma30 / 30f
            } else {
                // 前29根K线：数据不足，MA30无效
                point.mMA30Price = 0f
            }
            // ---------- MA60 ----------
            if (i == 59) {
                // 第60根K线（索引59）：首次凑齐60个数据，计算初始MA60
                point.mMA60Price = ma60 / 60f
            } else if (i >= 60) {
                // 后续K线：滑出最旧的一个收盘价，保持窗口大小为60
                ma60 -= data[i - 60].getClosePrice()
                point.mMA60Price = ma60 / 60f
            } else {
                // 前59根K线：数据不足，MA60无效
                point.mMA60Price = 0f
            }
        }
    }

    /**
     * 计算所有技术指标的统一入口
     * 按依赖顺序依次调用各指标计算方法，确保被依赖的指标（如MA）先于依赖它的指标（如BOLL）计算
     */
    fun calculate(data: MutableList<KLineChartBean>?) {
        data ?: return
        // MA必须最先计算，BOLL等指标依赖MA20的值
        calculateMA(data)
        calculateMACD(data)
        calculateBOLL(data)
        calculateRSI(data)
        calculateKDJ(data)
        calculateWR(data)
        // 成交量均线独立于价格指标，最后计算
        calculateVolumeMA(data)
    }

    /**
     * 计算成交量均线（VOL MA5 / MA10）
     * 采用与价格 MA 相同的滑动窗口累加法，避免重复循环求和
     */
    private fun calculateVolumeMA(data: MutableList<KLineChartBean>) {
        // 两条成交量均线的滑动累加和
        var volumeMa5 = 0f
        var volumeMa10 = 0f
        for (i in data.indices) {
            val entry = data[i]
            // 将当前成交量累加到两条均线的和中
            volumeMa5 += entry.getVolume()
            volumeMa10 += entry.getVolume()
            // ---------- VOL MA5 ----------
            if (i == 4) {
                // 第5根K线（索引4）：首次凑齐5个数据，计算初始VOL MA5
                entry.mMA5Volume = (volumeMa5 / 5f)
            } else if (i > 4) {
                // 后续K线：滑出最旧的一个成交量，保持窗口大小为5
                volumeMa5 -= data[i - 5].getVolume()
                entry.mMA5Volume = volumeMa5 / 5f
            } else {
                // 前4根K线：数据不足，VOL MA5无效
                entry.mMA5Volume = 0f
            }
            // ---------- VOL MA10 ----------
            if (i == 9) {
                // 第10根K线（索引9）：首次凑齐10个数据，计算初始VOL MA10
                entry.mMA10Volume = volumeMa10 / 10f
            } else if (i > 9) {
                // 后续K线：滑出最旧的一个成交量，保持窗口大小为10
                volumeMa10 -= data[i - 10].getVolume()
                entry.mMA10Volume = volumeMa10 / 10f
            } else {
                // 前9根K线：数据不足，VOL MA10无效
                entry.mMA10Volume = 0f
            }
        }
    }

}