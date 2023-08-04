package com.github.fujianlian.klinechart.entity

/**
 * 成交量接口
 * Created by hjm on 2017/11/14 17:46.
 */
interface IVolume {
    /**
     * 开盘价
     */
    val openPrice: Float

    /**
     * 收盘价
     */
    val closePrice: Float

    /**
     * 成交量
     */
    val volume: Float

    /**
     * 五(月，日，时，分，5分等)均量
     */
    val mA5Volume: Float

    /**
     * 十(月，日，时，分，5分等)均量
     */
    val mA10Volume: Float
}