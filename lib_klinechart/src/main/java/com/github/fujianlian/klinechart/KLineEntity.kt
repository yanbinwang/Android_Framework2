package com.github.fujianlian.klinechart

import com.github.fujianlian.klinechart.entity.IKLine

/**
 * K线实体
 * Created by tifezh on 2016/5/16.
 */
class KLineEntity : IKLine {
    var DateL: Long? = null

    //当前期数
    var period = 0
    var date: String? = null
    override var openPrice = 0f
    override var highPrice = 0f
    override var lowPrice = 0f
    override var closePrice = 0f
    override var volume = 0f
    override var mA5Price = 0f
    override var mA10Price = 0f
    override var mA20Price = 0f
    override var mA30Price = 0f
    override var mA60Price = 0f
    override var dea = 0f
    override var dif = 0f
    override var macd = 0f
    override var k = 0f
    override var d = 0f
    override var j = 0f
    override var r = 0f
    override var rsi = 0f
    override var up = 0f
    override var mb = 0f
    override var dn = 0f
    override var mA5Volume = 0f
    override var mA10Volume = 0f
    override var digits = ""//位数
}