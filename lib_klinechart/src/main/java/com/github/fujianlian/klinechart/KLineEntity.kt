package com.github.fujianlian.klinechart

import com.github.fujianlian.klinechart.entity.IKLine

/**
 * K线实体
 * Created by tifezh on 2016/5/16.
 */
open class KLineEntity : IKLine {
    var Date: String? = null

    override var openPrice: Float = 0f
    override var highPrice: Float = 0f
    override var lowPrice: Float = 0f
    override var closePrice: Float = 0f
    override var mA5Price: Float = 0f
    override var mA10Price: Float = 0f
    override var mA20Price: Float = 0f
    override var mA30Price: Float = 0f
    override var mA60Price: Float = 0f
    override var up: Float = 0f
    override var mb: Float = 0f
    override var dn: Float = 0f
    override var k: Float = 0f
    override var d: Float = 0f
    override var j: Float = 0f
    override var dea: Float = 0f
    override var dif: Float = 0f
    override var macd: Float = 0f
    override var rsi: Float = 0f
    override var volume: Float = 0f
    override var mA5Volume: Float = 0f
    override var mA10Volume: Float = 0f
    override var r: Float = 0f
}