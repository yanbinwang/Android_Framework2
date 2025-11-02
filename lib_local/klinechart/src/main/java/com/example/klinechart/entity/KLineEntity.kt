package com.example.klinechart.entity

/**
 * K线实体
 */
class KLineEntity : IKLine {
    var mDate = ""
    var mOpen = 0f
    var mHigh = 0f
    var mLow = 0f
    var mClose = 0f
    var mVolume = 0f
    var mMA5Price = 0f
    var mMA10Price = 0f
    var mMA20Price = 0f
    var mMA30Price = 0f
    var mMA60Price = 0f
    var mMA5Volume = 0f
    var mMA10Volume = 0f
    var mDea = 0f
    var mDif = 0f
    var mMacd = 0f
    var mK = 0f
    var mD = 0f
    var mJ = 0f
    var mR = 0f
    var mRsi = 0f
    var mUp = 0f
    var mMb = 0f
    var mDn = 0f

    override fun getDate(): String {
        return mDate
    }

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

    override fun getUp(): Float {
        return mUp
    }

    override fun getMb(): Float {
        return mMb
    }

    override fun getDn(): Float {
        return mDn
    }

    override fun getDea(): Float {
        return mDea
    }

    override fun getDif(): Float {
        return mDif
    }

    override fun getMacd(): Float {
        return mMacd
    }

    override fun getK(): Float {
        return mK
    }

    override fun getD(): Float {
        return mD
    }

    override fun getJ(): Float {
        return mJ
    }

    override fun getRsi(): Float {
        return mRsi
    }

    override fun getVolume(): Float {
        return mVolume
    }

    override fun getMA5Volume(): Float {
        return mMA5Volume
    }

    override fun getMA10Volume(): Float {
        return mMA10Volume
    }

    override fun getR(): Float {
        return mR
    }

}