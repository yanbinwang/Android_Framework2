package com.example.klinechart.entity

/**
 * K线实体
 */
class KLineEntity : IKLine {
    var Date = ""
    var Open = 0f
    var High = 0f
    var Low = 0f
    var Close = 0f
    var Volume = 0f
    var MA5Price = 0f
    var MA10Price = 0f
    var MA20Price = 0f
    var MA30Price = 0f
    var MA60Price = 0f
    var MA5Volume = 0f
    var MA10Volume = 0f
    var dea = 0f
    var dif = 0f
    var macd = 0f
    var k = 0f
    var d = 0f
    var j = 0f
    var r = 0f
    var rsi = 0f
    var up = 0f
    var mb = 0f
    var dn = 0f

    override fun getDate(): String {
        return Date
    }

    override fun getOpenPrice(): Float {
        return Open
    }

    override fun getHighPrice(): Float {
        return High
    }

    override fun getLowPrice(): Float {
        return Low
    }

    override fun getClosePrice(): Float {
        return Close
    }

    override fun getMA5Price(): Float {
        return MA5Price
    }

    override fun getMA10Price(): Float {
        return MA10Price
    }

    override fun getMA20Price(): Float {
        return MA20Price
    }

    override fun getMA30Price(): Float {
        return MA30Price
    }

    override fun getMA60Price(): Float {
        return MA60Price
    }

    override fun getUp(): Float {
        return up
    }

    override fun getMb(): Float {
        return mb
    }

    override fun getDn(): Float {
        return dn
    }

    override fun getDea(): Float {
        return dea
    }

    override fun getDif(): Float {
        return dif
    }

    override fun getMacd(): Float {
        return macd
    }

    override fun getK(): Float {
        return k
    }

    override fun getD(): Float {
        return d
    }

    override fun getJ(): Float {
        return j
    }

    override fun getRsi(): Float {
        return rsi
    }

    override fun getVolume(): Float {
        return Volume
    }

    override fun getMA5Volume(): Float {
        return MA5Volume
    }

    override fun getMA10Volume(): Float {
        return MA10Volume
    }

    override fun getR(): Float {
        return r
    }

}