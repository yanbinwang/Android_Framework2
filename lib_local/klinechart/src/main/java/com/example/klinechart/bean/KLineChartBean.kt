package com.example.klinechart.bean

/**
 * K线实体
 */
class KLineChartBean : IKLine {
    // ========== 属于 IKLine ==========
    override var date: String = ""

    // ========== 属于 ICandle (价格) ==========
    override var openPrice: Float = 0f

    override var highPrice: Float = 0f

    override var lowPrice: Float = 0f

    override var closePrice: Float = 0f

    // ========== 属于 ICandle (成交量) ==========
    override var volume: Float = 0f

    override var ma5Volume: Float = 0f

    override var ma10Volume: Float = 0f

    // ========== 属于 ICandle (MA均线) ==========
    override var ma5Price: Float = 0f

    override var ma10Price: Float = 0f

    override var ma20Price: Float = 0f

    override var ma30Price: Float = 0f

    override var ma60Price: Float = 0f

    // ========== 属于 ICandle (BOLL) ==========
    override var up: Float = 0f

    override var mb: Float = 0f

    override var dn: Float = 0f

    // ========== 属于 IMACD ==========
    override var dif: Float = 0f

    override var dea: Float = 0f

    override var macd: Float = 0f

    // ========== 属于 IKDJ ==========
    override var k: Float = 0f

    override var d: Float = 0f

    override var j: Float = 0f

    // ========== 属于 IWR ==========
    override var wr: Float = 0f

    // ========== 属于 IRSI ==========
    override var rsi: Float = 0f

}