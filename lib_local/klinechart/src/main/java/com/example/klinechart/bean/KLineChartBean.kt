package com.example.klinechart.bean

/**
 * K线实体
 */
class KLineChartBean : IKLine {
    // ========== 属于 IKLine ==========
    var mDate = ""

    override fun getDate(): String {
        return mDate
    }

    // ========== 属于 ICandle (价格) ==========
    var mOpen = 0f
    var mHigh = 0f
    var mLow = 0f
    var mClose = 0f

    override fun getOpenPrice(): Float {
        return mOpen
    }

    override fun getHighPrice(): Float {
        return mHigh
    }

    override fun getLowPrice(): Float {
        return mLow
    }

    override fun getClosePrice(): Float {
        return mClose
    }

    // ========== 属于 ICandle (成交量) ==========
    var mVolume = 0f
    var mMA5Volume = 0f
    var mMA10Volume = 0f

    override fun getVolume(): Float {
        return mVolume
    }

    override fun getMA5Volume(): Float {
        return mMA5Volume
    }

    override fun getMA10Volume(): Float {
        return mMA10Volume
    }

    // ========== 属于 ICandle (MA均线) ==========
    var mMA5Price = 0f
    var mMA10Price = 0f
    var mMA20Price = 0f
    var mMA30Price = 0f
    var mMA60Price = 0f

    override fun getMA5Price(): Float {
        return mMA5Price
    }

    override fun getMA10Price(): Float {
        return mMA10Price
    }

    override fun getMA20Price(): Float {
        return mMA20Price
    }

    override fun getMA30Price(): Float {
        return mMA30Price
    }

    override fun getMA60Price(): Float {
        return mMA60Price
    }

    // ========== 属于 ICandle (BOLL) ==========
    var mUp = 0f
    var mMb = 0f
    var mDn = 0f

    override fun getUp(): Float {
        return mUp
    }

    override fun getMb(): Float {
        return mMb
    }

    override fun getDn(): Float {
        return mDn
    }

    // ========== 属于 IMACD ==========
    var mDif = 0f
    var mDea = 0f
    var mMacd = 0f

    override fun getDif(): Float {
        return mDif
    }

    override fun getDea(): Float {
        return mDea
    }

    override fun getMacd(): Float {
        return mMacd
    }

    // ========== 属于 IKDJ ==========
    var mK = 0f
    var mD = 0f
    var mJ = 0f

    override fun getK(): Float {
        return mK
    }

    override fun getD(): Float {
        return mD
    }

    override fun getJ(): Float {
        return mJ
    }

    // ========== 属于 IWR ==========
    var mWr = 0f

    override fun getWR(): Float {
        return mWr
    }

    // ========== 属于 IRSI ==========
    var mRsi = 0f

    override fun getRsi(): Float {
        return mRsi
    }

}