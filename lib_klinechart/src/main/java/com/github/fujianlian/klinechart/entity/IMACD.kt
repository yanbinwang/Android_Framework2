package com.github.fujianlian.klinechart.entity

/**
 * MACD指标(指数平滑移动平均线)接口
 * @see <a href="https://baike.baidu.com/item/MACD指标"/>相关说明</a>
 * Created by tifezh on 2016/6/10.
 */
interface IMACD {
    /**
     * DEA值
     */
    fun getDea(): Float

    /**
     * DIF值
     */
    fun getDif(): Float

    /**
     * MACD值
     */
    fun getMacd(): Float
}