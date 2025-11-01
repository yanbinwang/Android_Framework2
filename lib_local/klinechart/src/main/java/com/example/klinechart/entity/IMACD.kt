package com.example.klinechart.entity

/**
 * MACD指标(指数平滑移动平均线)接口
 * 相关说明: https://baike.baidu.com/item/MACD指标
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